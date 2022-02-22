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
import edu.uoc.pdp.db.entity.Administrator;
import edu.uoc.pdp.db.entity.Customer;
import edu.uoc.pdp.db.entity.Image;
import edu.uoc.pdp.db.entity.Question;
import edu.uoc.pdp.db.entity.Response;
import edu.uoc.pdp.db.entity.ResponseImage;
import edu.uoc.pdp.db.entity.ResponseStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QuestionHandlerTest {

    @Mock
    private QuestionDAO questionDAO;
    @Mock
    private ResponseDAO responseDAO;
    @Mock
    private SessionManager sessionManager;
    @Mock
    private CustomerDAO customerDAO;
    @Mock
    private UserDAO userDAO;
    @Mock
    private ResponseImageDAO responseImageDAO;
    @Mock
    private ImageHandler imageHandler;

    @InjectMocks
    private QuestionHandler questionHandler;


    @Test
    public void askQuestionMustSaveQuestion() {
        String title = "títol pregunta";
        String pregunta = "aquesta es la pregunta";

        String emailClient = "customer";

        when(sessionManager.getUserId()).thenReturn(emailClient);
        when(customerDAO.getByEmail(emailClient)).thenReturn(getCustomer());

        questionHandler.askQuestion(title, pregunta);

        ArgumentCaptor<Question> question = ArgumentCaptor.forClass(Question.class);

        verify(questionDAO, times(1)).save(question.capture());
        Question savedQuestion = question.getValue();

        assertNotNull(savedQuestion);
        assertEquals(title, savedQuestion.getTitle());
        assertEquals(pregunta, savedQuestion.getMessage());
        assertEquals(getCustomer().getId(), savedQuestion.getCustomer().getId());

    }

    @Test
    public void respondQuestionMustSaveResponse() {
        String questionId = "questionId";
        String resposta = "aquesta es la resposta";
        String emailClient = "customer";

        when(sessionManager.getUserId()).thenReturn(emailClient);
        when(questionDAO.getById(questionId)).thenReturn(getQuestion());
        when(userDAO.getByEmail(emailClient)).thenReturn(getAdministrator());

        questionHandler.answerQuestion(questionId, resposta);

        ArgumentCaptor<Response> response = ArgumentCaptor.forClass(Response.class);

        verify(responseDAO, times(1)).save(response.capture());
        Response savedResponse = response.getValue();

        assertNotNull(savedResponse);
        assertEquals(questionId, savedResponse.getQuestion().getId());
        assertEquals(resposta, savedResponse.getMessage());
        assertEquals(getAdministrator().getId(), savedResponse.getUser().getId());
    }

    @Test
    public void changeStatusMustSaveStatusChanged() {
        String responseId = "responseId";
        ResponseStatus status = ResponseStatus.APPROVED;
        when(responseDAO.getById(responseId)).thenReturn(getResponse());

        questionHandler.changeResponseStatus(responseId, status);

        ArgumentCaptor<Response> response = ArgumentCaptor.forClass(Response.class);
        verify(responseDAO, times(1)).save(response.capture());

        Response savedResponse = response.getValue();
        assertNotNull(savedResponse);
        assertEquals(getResponse().getId(), savedResponse.getId());
        assertEquals(status, savedResponse.getStatus());
    }

    @Test
    public void addImageMustSaveImageAndResponseImage() throws ImageException {
        UploadedFile imageTest = getUploadedFile();
        String responseId = "responseId";
        when(responseDAO.getById(responseId)).thenReturn(getResponse());
        when(sessionManager.getUserId()).thenReturn(getCustomer().getEmail());
        when(imageHandler.processImage(any())).thenReturn(getImage());

        questionHandler.addImageToResponse(responseId, imageTest);

        ArgumentCaptor<ResponseImage> responseImage = ArgumentCaptor.forClass(ResponseImage.class);
        verify(responseImageDAO, times(1)).save(responseImage.capture());

        ResponseImage savedResponseImage = responseImage.getValue();
        assertNotNull(savedResponseImage);
        assertEquals(getResponseImage().getResponse().getId(), savedResponseImage.getResponse().getId());
        assertEquals(getResponseImage().getImage().getId(), savedResponseImage.getImage().getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addImageThrowsAnExceptionWhenDifferentUserIsCalling() throws ImageException {
        UploadedFile imageTest = getUploadedFile();
        String responseId = "responseId";
        when(responseDAO.getById(responseId)).thenReturn(getResponse());
        when(sessionManager.getUserId()).thenReturn("anotherUser");

        questionHandler.addImageToResponse(responseId, imageTest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addImageThrowsAnExceptionWhenCustomerUpdatesApproved() throws ImageException {
        UploadedFile imageTest = getUploadedFile();
        String responseId = "responseId";
        Response response = getResponse();
        response.setStatus(ResponseStatus.APPROVED);
        when(responseDAO.getById(responseId)).thenReturn(response);
        when(sessionManager.getUserId()).thenReturn(getCustomer().getEmail());

        questionHandler.addImageToResponse(responseId, imageTest);
    }

    @Test
    public void listAllQuestionsShowsAllResponseCountToAdmin() {
        when(sessionManager.isAdmin()).thenReturn(true);
        when(questionDAO.getAll()).thenReturn(Collections.singletonList(getQuestion()));
        when(responseDAO.countResponses("questionId", false)).thenReturn(3L);

        List<ForumQuestion> result = questionHandler.listAllQuestions();

        assertEquals(1, result.size());
        assertEquals(getQuestion(), result.get(0).getQuestion());
        assertEquals(3L, result.get(0).getResponses());
    }

    @Test
    public void listAllQuestionsShowsApprovedResponseCountToCustomer() {
        when(sessionManager.isAdmin()).thenReturn(false);
        when(questionDAO.getAll()).thenReturn(Collections.singletonList(getQuestion()));
        when(responseDAO.countResponses("questionId", true)).thenReturn(3L);

        List<ForumQuestion> result = questionHandler.listAllQuestions();

        assertEquals(1, result.size());
        assertEquals(getQuestion(), result.get(0).getQuestion());
        assertEquals(3L, result.get(0).getResponses());
    }

    private Customer getCustomer() {
        Customer customer = new Customer();
        customer.setId("customer2Id");
        customer.setEmail("customer");

        return customer;
    }

    private Administrator getAdministrator() {
        Administrator admin = new Administrator();
        admin.setId("adminId");

        return admin;
    }

    private Question getQuestion() {
        Question question = new Question();
        question.setId("questionId");

        return question;
    }

    private Response getResponse() {
        Response response = new Response();
        response.setId("responseId");
        response.setUser(getCustomer());
        response.setStatus(ResponseStatus.PENDING);

        return response;
    }

    private Image getImage() {
        Image image = new Image();
        image.setId("imageId");

        return image;
    }

    private UploadedFile getUploadedFile() {
        byte[] bytes = {10};
        return new UploadedFile("Test", "fitxer", bytes);
    }

    private ResponseImage getResponseImage() {
        ResponseImage respImage = new ResponseImage();
        respImage.setId("responseImageId");
        Response response = new Response();
        response.setId("responseId");
        respImage.setResponse(response);
        Image image = new Image();
        image.setId("imageId");
        respImage.setImage(image);

        return respImage;
    }
}
