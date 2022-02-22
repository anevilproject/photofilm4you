package edu.uoc.pdp.web.bean.profile;

import edu.uoc.pdp.web.bean.BaseBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Optional;

import static edu.uoc.pdp.db.entity.UserRoles.ADMIN;
import static edu.uoc.pdp.db.entity.UserRoles.CUSTOMER;

@RequestScoped
@ManagedBean(name = "auth")
public class AuthBean extends BaseBean {

    private static final long serialVersionUID = 6834686541465376080L;

    private static final Logger log = LoggerFactory.getLogger(AuthBean.class);

    private String username;
    private String password;
    private String referer;

    /**
     * Logs in a user using the specified credentials using the underlying JAAS configuration.
     * <p>
     * If a referer was specified during form rendering the user will be redirected to that view after a successful login
     * otherwise a preferred view by role will be used.
     */
    public void login() {
        try {
            HttpServletRequest request = getRequest();
            request.login(username, password);

            if (StringUtils.isBlank(referer)) {
                redirectByRole();
            } else {
                redirect(referer);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            addMessage("loginForm", "Email o contrassenya incorrectes");
            redirect();
        }
    }

    /**
     * Invalidates the current session and removes the user credentials from the request context.
     */
    public void logout() {
        try {
            HttpServletRequest request = getRequest();
            request.getSession().invalidate();
            request.logout();
        } catch (ServletException e) {
            log.error(e.getMessage(), e);
        }
        redirect("index.xhtml");
    }

    public String getUsername() {
        if (username == null) {
            username = Optional.ofNullable(getRequest().getUserPrincipal()).map(Principal::getName).orElse("");
        }
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return "";
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void redirectByRole() {
        HttpServletRequest request = getRequest();

        if (request.isUserInRole(CUSTOMER)) {
            redirect("index.xhtml");
        }
        if (request.isUserInRole(ADMIN)) {
            redirect("/back/panelAdmin.xhtml");
        }
    }

    public boolean isCustomer() {
        return getRequest().isUserInRole(CUSTOMER);
    }

    public boolean isAdmin() {
        return getRequest().isUserInRole(ADMIN);
    }

    public boolean isAuthenticated() {
        return getRequest().getUserPrincipal() != null;
    }

    public String getReferer() {
        if (referer == null) {
            referer = getLocation();
        }
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }
}
