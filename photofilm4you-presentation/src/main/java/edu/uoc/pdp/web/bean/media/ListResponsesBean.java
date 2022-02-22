package edu.uoc.pdp.web.bean.media;

import edu.uoc.pdp.core.ejb.media.MediaFacade;
import edu.uoc.pdp.db.entity.Response;
import edu.uoc.pdp.db.entity.ResponseStatus;
import edu.uoc.pdp.web.bean.BaseBean;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.List;

@ViewScoped
@ManagedBean(name = "listResponsesBean")
public class ListResponsesBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = -4162145998233853655L;
    @EJB
    private MediaFacade mediaFacade;

    private List<Response> responses;
    private String id;

    private ResponseStatus status = ResponseStatus.PENDING;

    public List<Response> getResponses() {
        if (responses == null) {
            responses = mediaFacade.listAllResponsesToAccept();
        }
        return responses;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public ResponseStatus[] getStatusItems() {
        return ResponseStatus.values();
    }


    public void changeStatus() {
        mediaFacade.changeResponseStatus(id, status);
        addGlobalSuccessMessage((status == ResponseStatus.APPROVED ? "Resposta ACCEPTADA correctament." : "Resposta REBUTJADA correctament."));
        redirect();
    }

}
