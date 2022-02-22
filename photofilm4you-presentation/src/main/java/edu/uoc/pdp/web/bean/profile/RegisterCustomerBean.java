package edu.uoc.pdp.web.bean.profile;

import edu.uoc.pdp.core.ejb.profile.ProfileFacade;
import edu.uoc.pdp.db.entity.Customer;
import edu.uoc.pdp.web.bean.BaseBean;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Objects;

@ViewScoped
@ManagedBean(name = "registerCustomer")
public class RegisterCustomerBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = -9056399237460499611L;

    @EJB
    private ProfileFacade profileFacade;
    @NotBlank(message = "És necessari introduir la confirmació de la contrasenya")
    private String userPasswordRepeat;
    @Valid
    private Customer customer = new Customer();

    public void save() {
        if (!passwordsMatch()) {
            addMessage("registerCustomerForm:userPasswordRepeat", "Les contrasenyes no coincideixen");
        } else if (existsUserByEmail()) {
            addMessage("registerCustomerForm:email", "Ja existeix un usuari amb aquest correu.");
        } else {
            profileFacade.registerCustomer(customer);

            addGlobalSuccessMessage("T'has registrat correctament!");

            redirect("login.xhtml");
        }
    }

    public String getUserPasswordRepeat() {
        return "";
    }

    public void setUserPasswordRepeat(String userPasswordRepeat) {
        this.userPasswordRepeat = userPasswordRepeat;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    private boolean existsUserByEmail() {
        return profileFacade.existUser(customer.getEmail());
    }

    private boolean passwordsMatch() {
        return Objects.equals(customer.getPassword(), userPasswordRepeat);
    }
}

