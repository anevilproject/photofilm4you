package edu.uoc.pdp.core.ejb.rent;

import edu.uoc.pdp.core.exception.NoShoppingCartException;
import edu.uoc.pdp.core.exception.ProductNotAvailableException;
import edu.uoc.pdp.core.exception.RentCancellationException;
import edu.uoc.pdp.core.exception.RentPaymentException;
import edu.uoc.pdp.core.exception.RentPenalizationException;
import edu.uoc.pdp.core.model.cart.ShoppingCart;
import edu.uoc.pdp.db.entity.Rent;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Local
public interface RentFacade {

    /**
     * Queries the number of available units of a product between the specified dates
     *
     * @param from    Date from
     * @param to      Date to
     * @param product Product id
     * @return The number of available units
     */
    int getAvailableUnits(LocalDate from, LocalDate to, String product);

    /**
     * Creates a new shopping cart associated to the current http session
     *
     * @param from Date from
     * @param to   Date to
     */
    void createCart(LocalDate from, LocalDate to);

    /**
     * Returns the current shopping cart in the session if any
     *
     * @return An optional containing the current cart, empty if there is no cart in the session
     */
    Optional<ShoppingCart> getCart();

    /**
     * Adds the specified amount of units of a product to the active shopping cart
     *
     * @param product Product to be added
     * @param units   Number of units to be added
     */
    void addToCart(String product, int units) throws NoShoppingCartException, ProductNotAvailableException;

    /**
     * Removes a product from the current cart
     *
     * @param product Id of the product to be removed
     */
    void removeFromCart(String product) throws NoShoppingCartException;

    /**
     * Confirms the current cart and persists it as a rent into the database
     *
     * @throws NoShoppingCartException      If there is no cart or the cart is empty
     * @throws ProductNotAvailableException If at least one of the products in the cart is no longer available
     */
    String confirmCart() throws NoShoppingCartException, ProductNotAvailableException;

    /**
     * Retrieves a rent with pending payment and returns a payment url
     *
     * @param rentId Identifier of the rent to be paid for
     * @return A payment url to redirect to
     */
    String requestPayment(String rentId) throws RentPaymentException;

    /**
     * Cancels a rent by identifier. Items blocked by this booking will be considered available again during the
     * booking dates
     *
     * @param rentId Rent identifier
     * @throws RentCancellationException If the rent is already cancelled or is starting day is in the past
     */
    void cancel(String rentId) throws RentCancellationException;

    /**
     * Retrieves the cancel penalty amount for a Rent
     *
     * @param rent Rent instance
     */
    BigDecimal calculateCancelPenalty(Rent rent);

    /**
     * Retrieves all rents with pending cancellation payments
     *
     * @return A list with all matching rents
     */
    List<Rent> listPendingPenalizations();

    /**
     * List all rents
     *
     * @return A list with all rents
     */
    List<Rent> listAllRents();

    /**
     * List all future rents
     *
     * @return A list with all future rents
     */
    List<Rent> listFutureRents();

    /**
     * Marks the cancellation of the specified rent as paid
     *
     * @param rentId Rent identifier
     * @throws RentPenalizationException if the rent has no cancellation or it has already been paid for
     */
    void closePenalization(String rentId) throws RentPenalizationException;

    /**
     * Retrieves a rent by its identifier
     *
     * @param rentId Rent identifier
     * @return A matching rent if any
     * @throws javax.persistence.EntityNotFoundException if there's no rent matching by id
     */
    Rent getRent(String rentId);

    /**
     * Confirms the payment of a rent
     *
     * @param rentId Rent identifier
     */
    void confirmPayment(String rentId);
}
