package edu.uoc.pdp.web.bean.rent;

import edu.uoc.pdp.core.ejb.rent.RentFacade;
import edu.uoc.pdp.core.exception.NoShoppingCartException;
import edu.uoc.pdp.core.exception.ProductNotAvailableException;
import edu.uoc.pdp.core.model.availability.AvailableProduct;
import edu.uoc.pdp.core.model.cart.ShoppingCart;
import edu.uoc.pdp.db.entity.Product;
import edu.uoc.pdp.web.bean.BaseBean;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import static edu.uoc.pdp.core.utils.DateUtils.parse;

@ViewScoped
@ManagedBean(name = "cartBean")
public class CartBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = -404167159587047452L;

    @EJB
    private RentFacade rentFacade;

    @NotBlank
    @NotNull(message = "Aquest camp és obligatori")
    @Pattern(regexp = "\\d{2}/\\d{2}/\\d{4}", message = "Format de data invàlid, ex: 01/01/2020")
    private String from;
    @NotNull(message = "Aquest camp és obligatori")
    @Pattern(regexp = "\\d{2}/\\d{2}/\\d{4}", message = "Format de data invàlid, ex: 01/01/2020")
    private String to;
    @Min(1)
    @NotNull
    private Integer units;
    @NotNull
    private String productId;


    /**
     * Calculates the price for one unit of a product for the current cart's date range
     *
     * @param product Product to be valuated
     * @return The total price
     */
    public BigDecimal calculateProductPrice(Product product) {
        return new AvailableProduct(product, 1, getCartDays()).getTotalPriceFormatted();
    }

    /**
     * Creates a new cart using the specified dates
     */
    public void createCart() {
        LocalDate from = parse(this.from);
        LocalDate to = parse(this.to);

        if (datesAreValid(from, to)) {
            rentFacade.createCart(from, to);
        }
        redirect();
    }

    /**
     * Attempts to add a product to the currently active cart
     */
    public void addToCart() {
        try {
            rentFacade.addToCart(productId, units);
            addGlobalSuccessMessage("S'ha afegit el producte al carret!");
        } catch (NoShoppingCartException e) {
            addGlobalErrorMessage("S'han de seleccionar dates abans d'afegir un producte al carret!");
        } catch (ProductNotAvailableException e) {
            addGlobalErrorMessage("No s'han pogut afegir les unitats seleccionades al carret");
        }
        redirect();
    }

    /**
     * Attempts to remove a product from the currently active cart
     */
    public void removeFromCart() {
        try {
            rentFacade.removeFromCart(productId);
            addGlobalSuccessMessage("S'ha eliminat el producte del carret");
        } catch (NoShoppingCartException e) {
            addGlobalErrorMessage("No s'han seleccionat dates per al carret");
        }
        redirect();
    }

    /**
     * Validates user input dates such that:
     * * to >= from
     * * from >= today
     * * to < today + 364 days
     *
     * @param from Date from
     * @param to   Date to
     * @return {@code true} if the dates are valid, {@code false} otherwise
     */
    private boolean datesAreValid(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            return failValidation("dateForm:dateTo", "La data de fi ha de ser posterior a la data d'inici");
        } else if (from.isBefore(LocalDate.now())) {
            return failValidation("dateForm:dateFrom", "La data d'inici ha de ser posterior a avui");
        } else if (to.isAfter(LocalDate.now().plusDays(364))) {
            return failValidation("dateForm:dateTo", "No es poden fer reserves a més d'un any vista");
        }
        return true;
    }

    private boolean failValidation(String id, String message) {
        getFacesContext().validationFailed();
        addMessage(id, message);

        return false;
    }

    public String getFrom() {
        if (from == null) {
            from = rentFacade.getCart().map(ShoppingCart::getFromAsString).orElse(null);
        }
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        if (to == null) {
            to = rentFacade.getCart().map(ShoppingCart::getToAsString).orElse(null);
        }
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getUnits() {
        return units;
    }

    public void setUnits(Integer units) {
        this.units = units;
    }

    public boolean isCartActive() {
        return rentFacade.getCart().isPresent();
    }

    public ShoppingCart getCurrent() {
        return rentFacade.getCart().orElse(null);
    }

    public int getCartSize() {
        return rentFacade.getCart().map(ShoppingCart::size).orElse(0);
    }

    public String getCartDaysFormatted() {
        int days = getCartDays();

        return days == 0 ? "-" : String.valueOf(days);
    }

    private int getCartDays() {
        return rentFacade.getCart().map(ShoppingCart::getDuration).orElse(0);
    }
}
