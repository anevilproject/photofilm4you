package edu.uoc.pdp.core.ejb.media.handler;

import com.google.common.net.MediaType;
import edu.uoc.pdp.core.configuration.ConfigurationProperties;
import edu.uoc.pdp.core.dao.ImageDAO;
import edu.uoc.pdp.core.exception.ImageException;
import edu.uoc.pdp.core.model.file.UploadedFile;
import edu.uoc.pdp.db.entity.Image;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Singleton
public class ImageHandler {

    @Inject
    private ImageDAO imageDAO;
    @Inject
    private ConfigurationProperties properties;


    /**
     * Converts an uploaded file into an image entity and applies the corresponding type conversion and rescaling to fit
     * reasonable bounds to be displayed back in the website
     *
     * @param uploadedFile Uploaded image file
     * @return An image entity
     * @throws ImageException If the image is not valid
     */
    public Image processImage(UploadedFile uploadedFile) throws ImageException {
        try (ByteArrayInputStream input = new ByteArrayInputStream(uploadedFile.getContent());
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            BufferedImage image = ImageIO.read(input);

            if (image == null) {
                throw new ImageException();
            }
            image = convertToJpeg(image, uploadedFile.getContentType());
            image = scale(image);

            ImageIO.write(image, "jpg", output);

            Image entity = new Image();
            entity.setContent(output.toByteArray());

            return entity;
        } catch (IOException e) {
            throw new ImageException();
        }
    }

    /**
     * Retrieves an image by id
     *
     * @param imageId Image id
     * @return An image entity
     * @throws javax.persistence.EntityNotFoundException if no image is found
     */
    public Image getImage(String imageId) {
        return imageDAO.getById(imageId);
    }

    public void deleteImage(Image image) {
        if (image != null) {
            imageDAO.delete(image);
        }
    }

    private BufferedImage convertToJpeg(BufferedImage image, String contentType) {
        if (!isJpeg(contentType)) {
            BufferedImage converted = new BufferedImage(
                    image.getWidth(),
                    image.getHeight(),
                    BufferedImage.TYPE_INT_RGB);

            converted.createGraphics().drawImage(image, 0, 0, Color.WHITE, null);

            return converted;
        }
        return image;
    }

    private BufferedImage scale(BufferedImage image) {
        int maxWidth = properties.getMaxImageWidth();
        int maxHeight = properties.getMaxImageHeight();
        BufferedImage result = image;

        if (image.getWidth() > maxWidth) {
            result = Scalr.resize(result, Scalr.Mode.FIT_TO_WIDTH, maxWidth);
        }
        if (result.getHeight() > maxHeight) {
            result = Scalr.resize(result, Scalr.Mode.FIT_TO_HEIGHT, maxHeight);
        }
        return result;
    }

    @SuppressWarnings("UnstableApiUsage")
    private boolean isJpeg(String contentType) {
        return MediaType.parse(contentType).is(MediaType.JPEG);
    }
}
