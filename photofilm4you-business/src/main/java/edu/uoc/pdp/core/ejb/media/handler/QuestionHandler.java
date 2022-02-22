package edu.uoc.pdp.core.ejb.media.handler;


import edu.uoc.pdp.core.dao.CustomerDAO;
import edu.uoc.pdp.core.dao.QuestionDAO;
import edu.uoc.pdp.core.dao.ResponseDAO;
import edu.uoc.pdp.core.dao.ResponseImageDAO;
import edu.uoc.pdp.core.dao.UserDAO;
import edu.uoc.pdp.core.ejb.session.SessionManager;
import edu.uoc.pdp.core.exception.ImageException;
import edu.uoc.pdp.core.model.file.UploadedFile;
import edu.uoc.pdp.core.model.forum.ForumQuestion;
import edu.uoc.pdp.db.entity.Question;
import edu.uoc.pdp.db.entity.Response;
import edu.uoc.pdp.db.entity.ResponseImage;
import edu.uoc.pdp.db.entity.ResponseStatus;
import org.hibernate.Hibernate;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class QuestionHandler {

    @Inject
    private QuestionDAO questionDAO;
    @Inject
    private ResponseDAO responseDAO;
    @Inject
    private SessionManager sessionManager;
    @Inject
    private CustomerDAO customerDAO;
    @Inject
    private UserDAO userDAO;
    @Inject
    private ResponseImageDAO responseImageDAO;
    @Inject
    private ImageHandler imageHandler;


    /**
     * Ask a question creates a new question from a Customer
     *
     * @param title   title of the question given by the user
     * @param message message of the question
     * @return A new question created using the parameters above
     */
    @Transactional
    public Question askQuestion(String title, String message) {
        Question question = new Question();
        question.setTitle(title);
        question.setMessage(message);
        question.setCreated(LocalDateTime.now());
        question.setCustomer(customerDAO.getByEmail(sessionManager.getUserId()));

        return questionDAO.save(question);
    }

    /**
     * Responds a question. The responder might be a Customer or an Administrator
     *
     * @param questionId The Id from the question to respond
     * @param response   the response to the question above
     * @return returns a new Response object using the parameters above
     * @throws javax.persistence.EntityNotFoundException if there's no question matching by id
     */
    @Transactional
    public Response answerQuestion(String questionId, String response) {
        Question question = questionDAO.getById(questionId);
        Response answer = new Response();
        answer.setMessage(response);
        answer.setQuestion(question);
        answer.setUser(userDAO.getByEmail(sessionManager.getUserId()));
        answer.setStatus(sessionManager.isCustomer() ? ResponseStatus.PENDING : ResponseStatus.APPROVED);
        answer.setCreated(LocalDateTime.now());
        
        return responseDAO.save(answer);
    }

    /**
     * Returns all questions. Admins will see a count with all responses, the rest of users will see a count of approved
     * responses
     *
     * @return A list of questions
     */
    public List<ForumQuestion> listAllQuestions() {
        boolean onlyApproved = !sessionManager.isAdmin();

        return questionDAO.getAll().stream()
                .map(question ->
                        new ForumQuestion(question, responseDAO.countResponses(question.getId(), onlyApproved)))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a question by id
     *
     * @param questionId Question identifier
     * @return The matching question
     * @throws javax.persistence.EntityNotFoundException if no question is found by the specified id
     */
    public Question getQuestion(String questionId) {
        return questionDAO.getById(questionId);
    }

    /**
     * Appends an image to an existing response
     *
     * @param responseId Response identifier
     * @param image      Image file to be appended
     * @throws ImageException           If the supplied image is not valid
     * @throws IllegalArgumentException If a user is appending an image to a response written by another user or a customer
     *                                  tries to update an approved/rejected response
     */
    @Transactional
    public void addImageToResponse(String responseId, UploadedFile image) throws ImageException {
        Response response = responseDAO.getById(responseId);

        if (!sessionManager.getUserId().equals(response.getUser().getEmail())) {
            throw new IllegalArgumentException("A user can't append images to a response sent by another user!");
        }
        if (!sessionManager.isAdmin() && response.getStatus() != ResponseStatus.PENDING) {
            throw new IllegalArgumentException("A customer can only append images to a pending response!");
        }
        ResponseImage respImage = new ResponseImage();
        respImage.setResponse(response);
        respImage.setImage(imageHandler.processImage(image));
        responseImageDAO.save(respImage);
    }

    /**
     * Changes the status of a response to the specified one
     *
     * @param responseId Response id
     * @param status     New response status
     */
    @Transactional
    public void changeResponseStatus(String responseId, ResponseStatus status) {
        Response response = responseDAO.getById(responseId);
        response.setStatus(status);
        responseDAO.save(response);
    }

    /**
     * Lists all responses for a question. Customers will see all approved responses plus their own. Admins will see all
     * submitted responses
     *
     * @param questionId Question identifier
     * @return A list of responses
     */
    public List<Response> listAllQuestionResponses(String questionId) {
        String customerEmail = sessionManager.isCustomer() ? sessionManager.getUserId() : null;
        boolean onlyApproved = !sessionManager.isAdmin();

        List<Response> responses = responseDAO.findAllQuestionResponses(questionId, customerEmail, onlyApproved);
        responses.forEach(response -> Hibernate.initialize(response.getImages()));

        return responses;
    }

    public List<Response> listAllCustomerResponses() {
        return responseDAO.findAllCustomerResponses(sessionManager.getUserId());
    }

    public List<Response> listAllResponsesToAccept() {
        List<Response> responses = responseDAO.findAllResponsesToAccept();
        responses.forEach(response -> Hibernate.initialize(response.getImages()));
        return responses;
    }

}
