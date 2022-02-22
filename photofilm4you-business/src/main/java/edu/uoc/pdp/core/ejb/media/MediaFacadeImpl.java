package edu.uoc.pdp.core.ejb.media;

import edu.uoc.pdp.core.ejb.media.handler.ImageHandler;
import edu.uoc.pdp.core.ejb.media.handler.QuestionHandler;
import edu.uoc.pdp.core.ejb.media.handler.RatingHandler;
import edu.uoc.pdp.core.exception.ImageException;
import edu.uoc.pdp.core.model.file.UploadedFile;
import edu.uoc.pdp.core.model.forum.ForumQuestion;
import edu.uoc.pdp.db.entity.Image;
import edu.uoc.pdp.db.entity.ProductRating;
import edu.uoc.pdp.db.entity.Question;
import edu.uoc.pdp.db.entity.Response;
import edu.uoc.pdp.db.entity.ResponseStatus;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

import static edu.uoc.pdp.db.entity.UserRoles.ADMIN;
import static edu.uoc.pdp.db.entity.UserRoles.CUSTOMER;

@Stateless
@PermitAll
public class MediaFacadeImpl implements MediaFacade {


    @Inject
    private QuestionHandler questionHandler;
    @Inject
    private RatingHandler ratingHandler;
    @Inject
    private ImageHandler imageHandler;


    /**
     * {@inheritDoc}
     */
    @Override
    public Image getImage(String imageId) {
        return imageHandler.getImage(imageId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @RolesAllowed(CUSTOMER)
    public Question askQuestion(String title, String message) {
        return questionHandler.askQuestion(title, message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @RolesAllowed({ADMIN, CUSTOMER})
    public Response answerQuestion(String questionId, String response) {
        return questionHandler.answerQuestion(questionId, response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForumQuestion> listAllQuestions() {
        return questionHandler.listAllQuestions();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Question getQuestion(String questionId) {
        return questionHandler.getQuestion(questionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @RolesAllowed(CUSTOMER)
    public ProductRating rateProduct(String productId, Integer rate, String comment) {
        return ratingHandler.rateProduct(productId, rate, comment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ProductRating> listAllProductComments(String productId) {
        return ratingHandler.listAllProductComments(productId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @RolesAllowed({ADMIN, CUSTOMER})
    public void addImageToResponse(String responseId, UploadedFile image) throws ImageException {
        questionHandler.addImageToResponse(responseId, image);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @RolesAllowed(ADMIN)
    public void changeResponseStatus(String responseId, ResponseStatus status) {
        questionHandler.changeResponseStatus(responseId, status);

    }

    /**
     * List all Responses to a question
     *
     * @param questionId the question Id
     * @return a List with all the responses given to a question. The list isn't filtered by its status and ordered
     * by creation date descending
     */
    @Override
    public List<Response> listAllQuestionResponses(String questionId) {
        return questionHandler.listAllQuestionResponses(questionId);
    }

    @Override
    @RolesAllowed(CUSTOMER)
    public List<Response> listAllCustomerResponses() {
        return questionHandler.listAllCustomerResponses();
    }

    @Override
    @RolesAllowed(ADMIN)
    public List<Response> listAllResponsesToAccept() {
        return questionHandler.listAllResponsesToAccept();
    }

}
