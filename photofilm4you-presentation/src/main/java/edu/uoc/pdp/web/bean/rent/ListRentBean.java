package edu.uoc.pdp.web.bean.rent;

import edu.uoc.pdp.core.ejb.profile.ProfileFacade;
import edu.uoc.pdp.core.ejb.rent.RentFacade;
import edu.uoc.pdp.db.entity.Rent;
import edu.uoc.pdp.web.bean.BaseBean;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.List;

@ViewScoped
@ManagedBean(name = "listRentBean")
public class ListRentBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 8977441265258190176L;

    @EJB
    private RentFacade rentFacade;
    @EJB
    private ProfileFacade profileFacade;

    private List<Rent> activeRents;
    private List<Rent> rentsPendingRefund;
    private List<Rent> allRents;
    private List<Rent> futureRents;


    public List<Rent> getAllRents() {
        if (allRents == null) {
            allRents = rentFacade.listAllRents();
        }
        return allRents;
    }

    public void setAllRents(List<Rent> allRents) {
        this.allRents = allRents;
    }

    public List<Rent> getFutureRents() {
        if (futureRents == null) {
            futureRents = rentFacade.listFutureRents();
        }
        return futureRents;
    }

    public void setFutureRents(List<Rent> futureRents) {
        this.futureRents = futureRents;
    }

    public List<Rent> getRentsPendingRefund() {
        if (rentsPendingRefund == null) {
            rentsPendingRefund = rentFacade.listPendingPenalizations();
        }
        return rentsPendingRefund;
    }

    public void setRentsPendingRefund(List<Rent> rentsPendingRefund) {
        this.rentsPendingRefund = rentsPendingRefund;
    }

    public List<Rent> getActiveRents() {
        if (activeRents == null) {
            activeRents = profileFacade.listAllActiveRents();
        }
        return activeRents;
    }

    public void setActiveRents(List<Rent> activeRents) {
        this.activeRents = activeRents;
    }
}
