package edu.uoc.pdp.web.bean.media;

import edu.uoc.pdp.core.ejb.media.MediaFacade;
import edu.uoc.pdp.db.entity.ResponseStatus;
import edu.uoc.pdp.web.bean.BaseBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;

@ViewScoped
@ManagedBean(name = "responseBean")
public class ResponseBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = -4675322924329019348L;

    private static final Logger log = LoggerFactory.getLogger(ResponseBean.class);

    @EJB
    private MediaFacade mediaFacade;
    private String id;
    private ResponseStatus status = ResponseStatus.PENDING;

    public void save() {
        try {
            mediaFacade.addImageToResponse(id, getUploadedFile());
            addGlobalSuccessMessage("La imatge s'ha guardat correctament");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addGlobalErrorMessage("No s'ha pogut guardar la imatge");
        }
        redirect();
    }

    public void changeStatus() {
        try {
            mediaFacade.changeResponseStatus(id, status);
            addGlobalSuccessMessage("S'ha canviat l'estat de la resposta");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addGlobalErrorMessage("No s'ha pogut canviar l'estat");
        }
        redirect();
    }

    public ResponseStatus[] getStatusItems() {
        return ResponseStatus.values();
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
