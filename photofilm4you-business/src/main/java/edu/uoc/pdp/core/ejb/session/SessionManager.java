package edu.uoc.pdp.core.ejb.session;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static edu.uoc.pdp.db.entity.UserRoles.ADMIN;
import static edu.uoc.pdp.db.entity.UserRoles.CUSTOMER;

/**
 * Session bean bound to the users session.
 */
@SessionScoped
public class SessionManager implements Serializable {

    private static final long serialVersionUID = 5461648622003481983L;

    @Resource
    private EJBContext context;

    /**
     * Internal session map. Does not share elements with the actual HttpSession attribute map.
     */
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    /**
     * Fetches an attribute from the session map
     *
     * @param name Attribute name
     * @param type Attribute type
     * @return The indexed attribute if present, {@code null} otherwise
     */
    public <T> T getAttribute(String name, Class<T> type) {
        return type.cast(attributes.get(name));
    }

    /**
     * Stores an object in the session attribute map. The key is removed if the value is null.
     *
     * @param name  Attribute name
     * @param value Attribute value
     */
    public void setAttribute(String name, Object value) {
        if (value == null) {
            attributes.remove(name);
        } else {
            attributes.put(name, value);
        }
    }

    /**
     * Retrieves the identifier of the currently logged in user
     *
     * @return User email
     */
    public String getUserId() {
        return context.getCallerPrincipal().getName();
    }

    /**
     * Queries whether the user is in role customer or not
     *
     * @return {@code true} if the user has a customer role, {@code false} otherwise
     */
    public boolean isCustomer() {
        return context.isCallerInRole(CUSTOMER);
    }

    /**
     * Queries whether the user is in role admin or not
     *
     * @return {@code true} if the user has an admin role, {@code false} otherwise
     */
    public boolean isAdmin() {
        return context.isCallerInRole(ADMIN);
    }
}
