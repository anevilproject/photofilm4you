package edu.uoc.pdp.web.servlet;

import edu.uoc.pdp.core.ejb.media.MediaFacade;
import edu.uoc.pdp.db.entity.Image;

import javax.ejb.EJB;
import javax.persistence.EntityNotFoundException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/image/*")
public class ImageServlet extends HttpServlet {

    private static final long serialVersionUID = -2988290240587853058L;

    /**
     * All images are currently being stored as jpg see {@link edu.uoc.pdp.core.ejb.media.handler.ImageHandler}
     */
    public static final String IMAGE_MEDIA_TYPE = "image/jpeg";

    @EJB
    private MediaFacade mediaFacade;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String id = request.getPathInfo().substring(1);

        try {
            Image image = mediaFacade.getImage(id);
            response.setContentType(IMAGE_MEDIA_TYPE);
            response.setContentLength(image.getContent().length);
            response.getOutputStream().write(image.getContent());
        } catch (EntityNotFoundException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
