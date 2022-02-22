package edu.uoc.pdp.web.bean;

import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.servlet.RequestDispatcher;

@RequestScoped
@ManagedBean(name = "errorBean")
public class ErrorBean extends BaseBean {

    private static final long serialVersionUID = 6834686541465376080L;


    /**
     * Formats the stacktrace of the exception thrown and captured at a servlet level
     *
     * @return Exception stacktrace
     */
    public String getStackTrace() {
        Throwable error = getError();

        return error == null ? "" : ExceptionUtils.getStackTrace(getError());
    }

    /**
     * Checks if the servlet invocation ended in an exception
     *
     * @return {@code true} if there's a registered error {@code false} otherwise
     */
    public boolean isExceptionThrown() {
        return getError() != null;
    }

    private Throwable getError() {
        return (Throwable) getRequest().getAttribute(RequestDispatcher.ERROR_EXCEPTION);
    }
}
