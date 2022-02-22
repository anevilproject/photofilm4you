package edu.uoc.pdp.core.ejb.media.handler;

import edu.uoc.pdp.core.configuration.ConfigurationProperties;
import edu.uoc.pdp.core.dao.ImageDAO;
import edu.uoc.pdp.core.exception.ImageException;
import edu.uoc.pdp.core.model.file.UploadedFile;
import edu.uoc.pdp.db.entity.Image;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ImageHandlerTest {

    @Mock
    private ImageDAO imageDAO;
    @Mock
    private ConfigurationProperties properties;

    @InjectMocks
    private ImageHandler imageHandler;


    @Test
    public void processImageMustConvertToJpeg() throws IOException, ImageException {
        Image image = imageHandler.processImage(getImageFile());

        InputStream is = new BufferedInputStream(new ByteArrayInputStream(image.getContent()));
        String mimeType = URLConnection.guessContentTypeFromStream(is);

        assertEquals("image/jpeg", mimeType);
    }

    @Test
    public void processImageMustScaleResultAndPreserveAspectRatio() throws IOException, ImageException {
        when(properties.getMaxImageWidth()).thenReturn(150);
        when(properties.getMaxImageHeight()).thenReturn(50);

        Image result = imageHandler.processImage(getImageFile());

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(result.getContent()));
        assertEquals(50, image.getHeight());
        assertEquals(50, image.getWidth());
    }

    @Test
    public void processImageMustSetWhiteBackgroundWhenImageHasAlphaChannel() throws IOException, ImageException {
        when(properties.getMaxImageWidth()).thenReturn(150);
        when(properties.getMaxImageHeight()).thenReturn(50);

        Image result = imageHandler.processImage(getImageFile());

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(result.getContent()));
        assertEquals(Color.WHITE, new Color(image.getRGB(0, 0)));
    }

    @Test(expected = ImageException.class)
    public void processImageThrowsImageExceptionWhenFileIsNotImage() throws IOException, ImageException {
        imageHandler.processImage(getTextFile());
    }

    @Test
    public void getImageReturnsDAOImage() {
        Image stored = new Image();
        when(imageDAO.getById("imageId")).thenReturn(stored);

        Image image = imageHandler.getImage("imageId");

        assertSame(stored, image);
    }

    @Test
    public void deleteMustInvokeDAOWhenImageIsNotNull() {
        Image stored = new Image();

        imageHandler.deleteImage(stored);

        verify(imageDAO, times(1)).delete(stored);
    }

    @Test
    public void deleteMustNotInvokeDAOWhenImageIsNull() {
        imageHandler.deleteImage(null);

        verifyNoInteractions(imageDAO);
    }

    private UploadedFile getImageFile() throws IOException {
        InputStream is = getClass().getResourceAsStream("/test.png");

        return new UploadedFile("image/png", "test.png", IOUtils.toByteArray(is));
    }

    private UploadedFile getTextFile() throws IOException {
        InputStream is = getClass().getResourceAsStream("/test.txt");

        return new UploadedFile("plain/text", "test.txt", IOUtils.toByteArray(is));
    }
}
