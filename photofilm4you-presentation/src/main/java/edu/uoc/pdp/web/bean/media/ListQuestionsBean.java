package edu.uoc.pdp.web.bean.media;

import edu.uoc.pdp.core.ejb.media.MediaFacade;
import edu.uoc.pdp.core.model.forum.ForumQuestion;
import edu.uoc.pdp.web.bean.BaseBean;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.List;

@ViewScoped
@ManagedBean(name = "listQuestionsBean")
public class ListQuestionsBean extends BaseBean {

    private static final long serialVersionUID = -4162145998233853655L;

    @EJB
    private MediaFacade mediaFacade;

    private List<ForumQuestion> questions;

    public List<ForumQuestion> getQuestions() {
        if (questions == null) {
            questions = mediaFacade.listAllQuestions();
        }
        return questions;
    }
}
