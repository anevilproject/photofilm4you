package edu.uoc.pdp.web.bean.profile;

import edu.uoc.pdp.core.ejb.profile.ProfileFacade;
import edu.uoc.pdp.db.entity.Rent;
import edu.uoc.pdp.web.bean.BaseBean;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import java.io.Serializable;
import java.util.List;

@RequestScoped
@ManagedBean(name = "myRents")
public class ListMyRentsBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = -9056399237460499611L;

    @EJB
    private ProfileFacade profileFacade;

    protected List<Rent> rentList;

    /**
     * Get rents by email
     *
     * @return List of rents
     */
    public List<Rent> getRentList() {
        if (rentList == null) {
            rentList = profileFacade.listMyRents();
        }
        return rentList;
    }
    
}
