package edu.uoc.pdp.web.bean.profile;

import edu.uoc.pdp.core.ejb.profile.ProfileFacade;
import edu.uoc.pdp.db.entity.Administrator;
import edu.uoc.pdp.db.entity.Customer;
import edu.uoc.pdp.web.bean.BaseBean;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import java.io.Serializable;
import java.util.List;

@RequestScoped
@ManagedBean(name = "userBean")
public class ListUsersBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = -9056399237460499611L;

    @EJB
    private ProfileFacade profileFacade;

    protected List<Administrator> administratorList;
    protected List<Customer> customerList;

    /**
     * Get administrator
     *
     * @return List of administrators
     */
    public List<Administrator> getAdministratorList() {
        if (administratorList == null) {
            administratorList = profileFacade.listAdministrators();
        }
        return administratorList;
    }

    /**
     * Get administrator
     *
     * @return List of customers
     */
    public List<Customer> getCustomerList() {
        if (customerList == null) {
            customerList = profileFacade.listCustomers();
        }
        return customerList;
    }

}