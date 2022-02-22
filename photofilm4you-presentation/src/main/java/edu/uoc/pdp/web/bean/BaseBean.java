package edu.uoc.pdp.web.bean;

import edu.uoc.pdp.core.model.file.UploadedFile;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static edu.uoc.pdp.web.listener.RedirectPhaseListener.INPUT_VALUES;
import static edu.uoc.pdp.web.listener.RedirectPhaseListener.MESSAGES;
import static edu.uoc.pdp.web.listener.RedirectPhaseListener.VIEW_ROOT;

public abstract class BaseBean implements Serializable {

    private static final long serialVersionUID = 3213211593403170963L;

    private static final Logger log = LoggerFactory.getLogger(BaseBean.class);

    protected static final String ERROR_MSG_ID = "globalError";
    protected static final String SUCCESS_MSG_ID = "globalSuccess";
    protected static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final Pattern URL_PATTERN = Pattern.compile("^https?://.*");

    /**
     * Intended to store the source of form submits in order to implement PRG pattern
     */
    protected String source;
    private Part file;

    public String getSource() {
        if (source == null) {
            source = getLocation();
        }
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Part getFile() {
        return file;
    }

    public void setFile(Part file) {
        this.file = file;
    }

    /**
     * Builds a url string of the current location
     *
     * @return Current url
     */
    protected String getLocation() {
        HttpServletRequest request = getRequest();

        String result = request.getServletPath();
        String contextPath = request.getContextPath();
        String queryString = request.getQueryString();

        if (StringUtils.isNotBlank(queryString)) {
            result += "?" + queryString;
        }
        if (StringUtils.isNotBlank(contextPath)) {
            result = contextPath + result;
        }
        return result;
    }

    /**
     * Redirects back to the source of a form submit if it was set in the form
     */
    protected void redirect() {
        redirect(false);
    }

    /**
     * Redirects back to the source of a form submit while maintaining all submitted input values if specified
     *
     * @param saveInputs Whether to persist inputs or not
     */
    protected void redirect(boolean saveInputs) {
        if (StringUtils.isNotBlank(source)) {
            if (saveInputs) {
                saveViewRoot(getFacesContext());
                saveInputValues(getFacesContext());
            }
            redirect(source);
        }
    }

    /**
     * Redirects the user to a new view. It can also be an external url.
     * <p>
     * Messages and input values that were added to the current view will be saved in the session map to be restored by
     * {@link edu.uoc.pdp.web.listener.RedirectPhaseListener} before rendering the redirected view.
     *
     * @param view Url to redirect to. Relative and absolute urls are supported
     */
    protected void redirect(String view) {
        try {
            String path = view;

            if (!isExternal(path)) {
                String context = getRequest().getContextPath();

                if (StringUtils.isNotBlank(context) && !path.startsWith(context)) {
                    path = context + "/" + StringUtils.removeStart(path, "/");
                }
                saveMessages(getFacesContext());
            }
            getExternalContext().redirect(path);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Retrieves the current instance of the faces context
     *
     * @return Current faces context
     */
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    /**
     * Retrieves the current instance of the external context
     *
     * @return Current external context
     */
    protected ExternalContext getExternalContext() {
        return getFacesContext().getExternalContext();
    }

    /**
     * Adds a message faces directed to a ui component
     *
     * @param id      Client component id
     * @param message Message to be displayed
     */
    protected void addMessage(String id, String message) {
        getFacesContext().addMessage(id, new FacesMessage(message));
    }

    /**
     * Adds a global success message to be displayed via a global snackbar component
     *
     * @param message Message to be displayed
     */
    protected void addGlobalSuccessMessage(String message) {
        getFacesContext().addMessage(SUCCESS_MSG_ID, new FacesMessage(message));
    }

    /**
     * Adds a global error message to be displayed via a global snackbar component
     *
     * @param message Message to be displayed
     */
    protected void addGlobalErrorMessage(String message) {
        getFacesContext().addMessage(ERROR_MSG_ID, new FacesMessage(message));
    }

    /**
     * Retrieves the current http request
     *
     * @return Active {@link HttpServletRequest}
     */
    protected HttpServletRequest getRequest() {
        return (HttpServletRequest) getExternalContext().getRequest();
    }

    protected UploadedFile getUploadedFile() throws IOException {
        if (file == null) {
            return null;
        }
        return new UploadedFile(file.getContentType(), file.getName(), readFile());
    }

    public byte[] readFile() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BufferedInputStream bis = new BufferedInputStream(file.getInputStream());
        for (int b; (b = bis.read()) != -1; ) {
            if (bos.size() > MAX_FILE_SIZE) {
                throw new RuntimeException("El fitxer seleccionat es massa gran (> " + getMaxFileSizeFormatted() + ")");
            }
            bos.write(b);
        }
        return bos.toByteArray();
    }

    public long getMaxFileSize() {
        return MAX_FILE_SIZE;
    }

    public String getMaxFileSizeFormatted() {
        return (MAX_FILE_SIZE / (1024 * 1024)) + "MB";
    }

    private boolean isExternal(String url) {
        return URL_PATTERN.matcher(url).matches();
    }

    private void saveViewRoot(FacesContext context) {
        context.getExternalContext().getSessionMap().put(VIEW_ROOT, context.getViewRoot());
    }

    private void saveMessages(FacesContext context) {
        Map<String, Object> sessionMap = getExternalContext().getSessionMap();
        Map<String, List<FacesMessage>> messageMap = new LinkedHashMap<>();

        for (Iterator<String> it = getFacesContext().getClientIdsWithMessages(); it.hasNext(); ) {
            String clientId = it.next();

            messageMap.put(clientId, new ArrayList<>(context.getMessageList(clientId)));
        }
        sessionMap.put(MESSAGES, messageMap);
    }

    private void saveInputValues(FacesContext context) {
        Map<String, Object> inputValues = new HashMap<>();
        context.getExternalContext().getSessionMap().put(INPUT_VALUES, inputValues);

        saveInputValues(context, context.getViewRoot().getChildren(), inputValues);
    }

    private void saveInputValues(FacesContext facesContext, List<UIComponent> children, Map<String, Object> inputValues) {
        for (UIComponent component : children) {
            if (component instanceof UIInput) {
                UIInput input = (UIInput) component;
                inputValues.put(input.getClientId(facesContext), input.getValue());
            }
            saveInputValues(facesContext, component.getChildren(), inputValues);
        }
    }
}
