package edu.uoc.pdp.web.bean.profile;

import edu.uoc.pdp.core.ejb.profile.ProfileFacade;
import edu.uoc.pdp.db.entity.Customer;
import edu.uoc.pdp.web.bean.BaseBean;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.validation.Valid;
import java.io.Serializable;
import java.security.Principal;
import java.util.Objects;
import java.util.Optional;

@ViewScoped
@ManagedBean(name = "updateCustomerBean")
public class UpdateCustomerBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = -9056399237460499611L;

    @EJB
    private ProfileFacade profileFacade;

    private String username;
    @Valid
    private Customer customer;
    private String password;
    private String userPasswordRepeat;

    public void save() {
        if (isPasswordValid()) {
            if (password.length() > 0) {
                customer.setPassword(password);
            } else {
                customer.setPassword(null);
            }

            profileFacade.updateCustomerData(customer);

            addGlobalSuccessMessage("Perfil actualitzat correctament!");

            redirect("customer/showCustomer.xhtml");
        }
    }

    public boolean isPasswordValid() {
        boolean esValid = true;

        if (password.length() > 0 && !passwordsMatch()) {
            addMessage("updateCustomerForm:userPasswordRepeat", "Les contrasenyes no coincideixen");
            esValid = false;
        }

        if (password.length() > 0 && (password.length() < 4 || password.length() > 30)) {
            addMessage("updateCustomerForm:password", "La contrasenya ha de tenir 4 o més caràcters i menys de 30");
            esValid = false;
        }

        return esValid;
    }

    public Customer getCustomer() {
        if (customer == null) {
            customer = profileFacade.getCustomerByEmail(getUsername());
        }
        return customer;
    }

    public boolean passwordsMatch() {
        return Objects.equals(password, userPasswordRepeat);
    }

    public String getUsername() {
        if (username == null) {
            username = Optional.ofNullable(getRequest().getUserPrincipal()).map(Principal::getName).orElse("");
        }
        return username;
    }

    public String getPassword() {
        return "";
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserPasswordRepeat() {
        return "";
    }

    public void setUserPasswordRepeat(String userPasswordRepeat) {
        this.userPasswordRepeat = userPasswordRepeat;
    }
}


