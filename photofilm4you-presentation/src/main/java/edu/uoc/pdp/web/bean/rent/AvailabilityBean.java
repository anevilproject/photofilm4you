package edu.uoc.pdp.web.bean.rent;

import edu.uoc.pdp.core.ejb.rent.RentFacade;
import edu.uoc.pdp.db.entity.Product;
import edu.uoc.pdp.web.bean.BaseBean;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static edu.uoc.pdp.core.utils.DateUtils.format;

@RequestScoped
@ManagedBean(name = "availabilityBean")
public class AvailabilityBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 8977441265258190176L;

    @EJB
    private RentFacade rentFacade;

    private final Map<String, Integer> availabilityMap = new HashMap<>();

    /**
     * Returns the available units of a specified product for the currently active date range.
     * <p>
     * Available units are locally cached during view rendering time
     *
     * @param product Product
     * @return Number of available units
     */
    public int getAvailableUnits(Product product) {
        return availabilityMap.computeIfAbsent(product.getId(), this::fetchUnits);
    }

    /**
     * Returns the maximum available day. No rents are accepted beyond a year from now
     *
     * @return Formatted max date
     */
    public String getMaxDate() {
        return format(LocalDate.now().plusDays(364));
    }

    /**
     * Returns the minimum available day. No rents are accepted before today
     *
     * @return Formatted min date
     */
    public String getMinDate() {
        return format(LocalDate.now());
    }

    /**
     * Checks whether the current user can add more units of a product to the active cart.
     *
     * @param product Product
     * @return {@code true} if more items can be added, {@code false} otherwise
     */
    public boolean canAddUnits(Product product) {
        return getMaxUnits(product) > 0;
    }

    /**
     * Returns an array of unit options that the user can add to the cart for a given product
     *
     * @param product Product
     * @return An int array containing available options
     */
    public int[] getAddToCartUnits(Product product) {
        int max = getMaxUnits(product);

        if (max == 0) {
            return new int[]{0};
        }
        return IntStream.rangeClosed(1, max).toArray();
    }

    private int getMaxUnits(Product product) {
        return Math.max(getAvailableUnits(product) - getCartUnits(product), 0);
    }

    private int getCartUnits(Product product) {
        return rentFacade.getCart().map(cart -> cart.getUnitsOfProduct(product.getId())).orElse(0);
    }

    private int fetchUnits(String product) {
        return rentFacade.getCart()
                .map(cart -> rentFacade.getAvailableUnits(cart.getFrom(), cart.getTo(), product))
                .orElse(0);
    }
}
