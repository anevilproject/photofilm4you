package edu.uoc.pdp.web.listener;

import org.apache.commons.collections4.MapUtils;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class RedirectPhaseListener implements PhaseListener {

    private static final long serialVersionUID = -5576555754581636548L;

    public static final String MESSAGES = "redirect_messages";
    public static final String INPUT_VALUES = "redirect_input_values";
    public static final String VIEW_ROOT = "view_root";

    @Override
    public void afterPhase(PhaseEvent event) {
        // Do nothing
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        FacesContext facesContext = event.getFacesContext();
        Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();

        restoreViewRoot(facesContext, sessionMap);
        restoreMessages(facesContext, sessionMap);
        restoreInputValues(facesContext, sessionMap);
    }

    private void restoreViewRoot(FacesContext facesContext, Map<String, Object> sessionMap) {
        UIViewRoot root = (UIViewRoot) sessionMap.remove(VIEW_ROOT);

        if (root != null) {
            facesContext.setViewRoot(root);
        }
    }

    private void restoreMessages(FacesContext facesContext, Map<String, Object> sessionMap) {
        Map<String, List<FacesMessage>> messageMap = (Map<String, List<FacesMessage>>) sessionMap.remove(MESSAGES);

        if (MapUtils.isNotEmpty(messageMap)) {
            messageMap.forEach((client, messages) -> messages.forEach(message -> facesContext.addMessage(client, message)));
        }
    }

    private void restoreInputValues(FacesContext facesContext, Map<String, Object> sessionMap) {
        Map<String, Object> inputValues = (Map<String, Object>) sessionMap.remove(INPUT_VALUES);

        if (MapUtils.isNotEmpty(inputValues)) {
            for (Map.Entry<String, Object> entry : inputValues.entrySet()) {
                UIInput input = (UIInput) facesContext.getViewRoot().findComponent(entry.getKey());
                input.setValue(entry.getValue());
            }
        }
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RENDER_RESPONSE;
    }
}
