package edu.uoc.pdp.web.bean.media;

import edu.uoc.pdp.core.ejb.media.MediaFacade;
import edu.uoc.pdp.db.entity.Question;
import edu.uoc.pdp.db.entity.Response;
import edu.uoc.pdp.db.entity.ResponseStatus;
import edu.uoc.pdp.web.bean.BaseBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

import static edu.uoc.pdp.db.entity.UserRoles.ADMIN;
import static edu.uoc.pdp.db.entity.UserRoles.CUSTOMER;

@ViewScoped
@ManagedBean(name = "showQuestionBean")
public class ShowQuestionBean extends BaseBean {

    private static final long serialVersionUID = -4675322924329019348L;

    private static final Logger log = LoggerFactory.getLogger(ShowQuestionBean.class);

    @EJB
    private MediaFacade mediaFacade;

    @Valid
    private Question question;
    private List<Response> responses;
    private List<Response> customerResponses;
    private String id;
    @Size(max = 2000, message = "La resposta no pot tenir més de 2000 caràcters")
    @NotBlank(message = "La resposta no pot estar buida")
    private String responseMessage;


    /**
     * Saves a new question
     */
    public void save() {
        try {
            mediaFacade.askQuestion(question.getTitle(), question.getMessage());
            addGlobalSuccessMessage("La pregunta s'ha enviat correctament");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addGlobalErrorMessage("No s'ha pogut enviar la pregunta");
        }
        redirect();
    }

    /**
     * Saves a response for a question
     */
    public void postResponse() {
        HttpServletRequest request = getRequest();
        try {
            mediaFacade.answerQuestion(id, responseMessage);
            if (request.isUserInRole(CUSTOMER)) {
                addGlobalSuccessMessage("La resposta s'ha guardat correctament i resta pendent d'aprovació pels moderadors del fòrum");
            }
            if (request.isUserInRole(ADMIN)) {
                addGlobalSuccessMessage("La resposta s'ha guardat correctament");
            }
            redirect();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addGlobalErrorMessage("No s'ha pogut guardar la resposta");
            redirect(true);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Response> getResponses() {
        if (responses == null) {
            loadResponses();
        }
        return responses;
    }

    public void setResponses(List<Response> responses) {
        this.responses = responses;
    }

    public Question getQuestion() {
        if (question == null) {
            question = id == null ? new Question() : mediaFacade.getQuestion(id);
        }
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public List<Response> getCustomerResponses() {
        if (customerResponses == null) {
            loadResponses();
        }
        return customerResponses;
    }

    public void setCustomerResponses(List<Response> customerResponses) {
        this.customerResponses = customerResponses;
    }

    private void loadResponses() {
        HttpServletRequest request = getRequest();
        String userId = request.isUserInRole(CUSTOMER) ? request.getUserPrincipal().getName() : "";

        responses = new ArrayList<>();
        customerResponses = new ArrayList<>();

        for (Response response : mediaFacade.listAllQuestionResponses(id)) {
            if (response.getStatus() != ResponseStatus.APPROVED && userId.equals(response.getUser().getEmail())) {
                customerResponses.add(response);
            } else {
                responses.add(response);
            }
        }
    }
}
