package edu.uoc.pdp.core.ejb.media;

import edu.uoc.pdp.core.exception.ImageException;
import edu.uoc.pdp.core.model.file.UploadedFile;
import edu.uoc.pdp.core.model.forum.ForumQuestion;
import edu.uoc.pdp.db.entity.Image;
import edu.uoc.pdp.db.entity.ProductRating;
import edu.uoc.pdp.db.entity.Question;
import edu.uoc.pdp.db.entity.Response;
import edu.uoc.pdp.db.entity.ResponseStatus;

import javax.ejb.Local;
import java.util.List;

@Local
public interface MediaFacade {

    /**
     * Retrieves an image by id
     *
     * @param imageId Image id
     * @return An image entity
     * @throws javax.persistence.EntityNotFoundException if no image is found
     */
    Image getImage(String imageId);

    /**
     * Ask a question creates a new question from a Customer
     *
     * @param title   title of the question given by the user
     * @param message message of the question
     * @return A new question created using the parameters above
     */
    Question askQuestion(String title, String message);

    /**
     * Responds a question. The responder might be a Customer or an Administrator
     *
     * @param questionId The Id from the question to respond
     * @param response   the response to the question above
     * @return returns a new Response object using the parameters above
     * @throws javax.persistence.EntityNotFoundException if there's no question matching by id
     */
    Response answerQuestion(String questionId, String response);

    /**
     * List all questions available in the system TODO is there any filter needed?
     *
     * @return A list with all the questions available
     */
    List<ForumQuestion> listAllQuestions();

    /**
     * Retrieves a Question by its Id
     *
     * @param questionId the Id from the question requested
     * @return a Question object
     * @throws javax.persistence.EntityNotFoundException if there's no question matching by id
     */
    Question getQuestion(String questionId);

    /**
     * Rate a product, creating a new instance of ProductRating
     *
     * @param productId the product Id
     * @param rate      rate from 0 to 5 TODO implement this filter somewhere
     * @param comment   a String with the comment
     * @throws javax.persistence.EntityNotFoundException if there's no product matching by id
     */
    ProductRating rateProduct(String productId, Integer rate, String comment);

    /**
     * List all comments from a product TODO is filtered by a product Id?
     *
     * @param productId the product Id from we want to know its comments
     * @return A list with all the ProductRating available
     * @throws javax.persistence.EntityNotFoundException if there's no product matching by id
     */
    List<ProductRating> listAllProductComments(String productId);

    /**
     * Add an image to a response
     *
     * @param responseId responseID where we want to add an image
     * @param image      image object to be added
     * @throws javax.persistence.EntityNotFoundException if there's no response matching by id
     */
    void addImageToResponse(String responseId, UploadedFile image) throws ImageException;

    /**
     * Accept or reject a Response made by a customer (Administrators only)
     * formerly known as acceptOrRejectResponse
     *
     * @param responseId The id from the response pending to be accepted
     * @param status     ResponseStatus to be updated
     * @throws javax.persistence.EntityNotFoundException if there's no response matching by id
     */
    void changeResponseStatus(String responseId, ResponseStatus status);

    /**
     * List all Responses to a question. Admins will see all responses and customers will only get approved responses and
     * their own responses
     *
     * @param questionId the question Id
     * @return a List with all the responses given to a question. The list isn't filtered by its status and ordered
     * by creation date descending
     */
    List<Response> listAllQuestionResponses(String questionId);

    /**
     * @return all customer responses
     */
    List<Response> listAllCustomerResponses();

    /**
     * @return all customer responses to accept
     */
    List<Response> listAllResponsesToAccept();

}
