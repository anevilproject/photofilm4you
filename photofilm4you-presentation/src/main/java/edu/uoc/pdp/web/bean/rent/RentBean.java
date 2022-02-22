package edu.uoc.pdp.web.bean.rent;

import edu.uoc.pdp.core.ejb.rent.RentFacade;
import edu.uoc.pdp.core.exception.NoShoppingCartException;
import edu.uoc.pdp.core.exception.ProductNotAvailableException;
import edu.uoc.pdp.core.exception.RentCancellationException;
import edu.uoc.pdp.core.exception.RentPenalizationException;
import edu.uoc.pdp.core.utils.DateUtils;
import edu.uoc.pdp.db.entity.Rent;
import edu.uoc.pdp.web.bean.BaseBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import static edu.uoc.pdp.core.utils.DateUtils.format;

@ViewScoped
@ManagedBean(name = "rentBean")
public class RentBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 8977441265258190176L;

    private static final Logger log = LoggerFactory.getLogger(RentBean.class);

    @EJB
    private RentFacade rentFacade;

    private String referer;
    private String rentId;
    private Rent rent;


    /**
     * Confirms the current user cart and redirects to the resulting payment url
     */
    public void confirm() {
        String result = null;

        try {
            result = rentFacade.confirmCart();
        } catch (NoShoppingCartException e) {
            addGlobalErrorMessage("No s'han seleccionat les dates del lloguer!");
        } catch (ProductNotAvailableException e) {
            addGlobalErrorMessage("No s'ha pogut confirmar. S'ha eliminat el producte sense stock del carret.");
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            addGlobalErrorMessage("No s'ha pogut confirmar el lloguer :(");
        }
        if (result == null) {
            redirect();
        } else {
            redirect(result);
        }
    }

    /**
     * Cancels a rent by identifier
     */
    public void cancel() {
        try {
            rentFacade.cancel(rentId);
            addGlobalSuccessMessage("S'ha cancel·lat el lloguer. Es procedirà amb la devolució de l'import corresponent.");
        } catch (RentCancellationException e) {
            addGlobalErrorMessage("No es pot cancel·lar el lloguer.");
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            addGlobalErrorMessage("No s'ha pogut cancel·lar el lloguer :(");
        }
        redirect();
    }

    /**
     * Marks a rent pending refund as refunded
     */
    public void refund() {
        try {
            rentFacade.closePenalization(rentId);
            addGlobalSuccessMessage("S'ha marcat la devolució com a completada.");
        } catch (RentPenalizationException e) {
            addGlobalErrorMessage("No es pot completar la devolució d'aquesta reserva.");
        }
        redirect();
    }

    /**
     * Starts the payment process of an unpaid rent
     */
    public void requestPayment() {
        try {
            redirect(rentFacade.requestPayment(rentId));
        } catch (Exception e) {
            addGlobalErrorMessage("No s'ha pogut iniciar el pagament de la reserva");
            redirect();
        }
    }

    /**
     * Invokes a rent search by identified
     */
    public void search() {
        try {
            if (StringUtils.isNotBlank(rentId)) {
                getRent();
                redirect("/back/rent/showRent.xhtml?rentId=" + rentId + "&referer=/back/panelAdmin");
            }
        } catch (Exception e) {
            log.warn("Rent {} not found!", rentId);
        }
        if (rent == null) {
            addGlobalErrorMessage("No s'ha trobat el lloguer especificat.");
            redirect();
        }
    }

    /**
     * Calculates the cancellation penalty for a rent
     *
     * @param rent Rent instance
     * @return Penalty amount
     */
    public BigDecimal calculateCancelPenalty(Rent rent) {
        return rentFacade.calculateCancelPenalty(rent);
    }

    public String formatCreationDate(Rent rent) {
        return DateUtils.formatCreationDate(rent.getCreated());
    }

    public String formatCancellationDate(Rent rent) {
        if (rent.getCancellation() == null) {
            return "";
        }
        return DateUtils.formatCancellationDate(rent.getCancellation().getCreationDate());
    }

    public String getRentId() {
        return rentId;
    }

    public void setRentId(String rentId) {
        this.rentId = rentId;
    }

    public Rent getRent() {
        if (rent == null) {
            rent = rentFacade.getRent(rentId);
        }
        return rent;
    }

    public void setRent(Rent rent) {
        this.rent = rent;
    }

    public String formatDate(LocalDate date) {
        return format(date);
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }
}
