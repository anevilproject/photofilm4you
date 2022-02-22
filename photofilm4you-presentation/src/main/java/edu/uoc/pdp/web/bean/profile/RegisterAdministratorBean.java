package edu.uoc.pdp.web.bean.profile;

import edu.uoc.pdp.core.ejb.profile.ProfileFacade;
import edu.uoc.pdp.db.entity.Administrator;
import edu.uoc.pdp.web.bean.BaseBean;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Objects;

@ViewScoped
@ManagedBean(name = "administrator")
public class RegisterAdministratorBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = -9056399237460499611L;

    @EJB
    private ProfileFacade profileFacade;
    @NotBlank(message = "És necessari introduir la confirmació de la contrasenya")
    private String userPasswordRepeat;
    @Valid
    private Administrator administrator = new Administrator();

    public void save() {
        if (!passwordsMatch()) {
            addMessage("registerAdmForm:userPasswordRepeat", "Les contrasenyes no coincideixen");
        } else if (existsUserByEmail()) {
            addMessage("registerAdmForm:email", "Ja existeix un usuari amb aquest correu.");
        } else {
            profileFacade.registerAdministrator(administrator);

            addGlobalSuccessMessage("Administrador creat correctament.");
            redirect("/back/userList.xhtml");
        }
    }

    public boolean existsUserByEmail() {
        return profileFacade.existUser(administrator.getEmail());
    }

    public boolean passwordsMatch() {
        return Objects.equals(administrator.getPassword(), userPasswordRepeat);
    }

    public Administrator getAdministrator() {
        return administrator;
    }

    public void setAdministrator(Administrator administrator) {
        this.administrator = administrator;
    }

    public String getUserPasswordRepeat() {
        return "";
    }

    public void setUserPasswordRepeat(String userPasswordRepeat) {
        this.userPasswordRepeat = userPasswordRepeat;
    }
}

