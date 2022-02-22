package edu.uoc.pdp.core.ejb.rent;

import edu.uoc.pdp.core.ejb.rent.cache.ProductAvailabilityCache;
import edu.uoc.pdp.core.ejb.rent.handler.RentHandler;
import edu.uoc.pdp.core.ejb.rent.handler.ShoppingCartHandler;
import edu.uoc.pdp.core.exception.NoShoppingCartException;
import edu.uoc.pdp.core.exception.ProductNotAvailableException;
import edu.uoc.pdp.core.exception.RentCancellationException;
import edu.uoc.pdp.core.exception.RentPaymentException;
import edu.uoc.pdp.core.exception.RentPenalizationException;
import edu.uoc.pdp.core.model.cart.ShoppingCart;
import edu.uoc.pdp.db.entity.Rent;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static edu.uoc.pdp.db.entity.UserRoles.ADMIN;
import static edu.uoc.pdp.db.entity.UserRoles.CUSTOMER;

@Stateless
@PermitAll
public class RentFacadeImpl implements RentFacade {

    @Inject
    private ProductAvailabilityCache availabilityCache;
    @Inject
    private ShoppingCartHandler cartHandler;
    @Inject
    private RentHandler rentHandler;


    /**
     * {@inheritDoc}
     */
    @Override
    public int getAvailableUnits(LocalDate from, LocalDate to, String product) {
        return availabilityCache.getAvailableProduct(from, to, product).getUnits().get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ShoppingCart> getCart() {
        return cartHandler.getCartIfPresent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RolesAllowed(CUSTOMER)
    public void createCart(LocalDate from, LocalDate to) {
        cartHandler.createCart(from, to);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RolesAllowed(CUSTOMER)
    public void addToCart(String product, int units) throws NoShoppingCartException, ProductNotAvailableException {
        cartHandler.addToCart(product, units);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RolesAllowed(CUSTOMER)
    public void removeFromCart(String product) throws NoShoppingCartException {
        cartHandler.removeFromCart(product);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @RolesAllowed(CUSTOMER)
    public String confirmCart() throws NoShoppingCartException, ProductNotAvailableException {
        String result = rentHandler.confirm(cartHandler.mapCartToRent());

        cartHandler.deleteCart();

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @RolesAllowed(CUSTOMER)
    public String requestPayment(String rentId) throws RentPaymentException {
        return rentHandler.requestPayment(rentId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @RolesAllowed({CUSTOMER, ADMIN})
    public void cancel(String rentId) throws RentCancellationException {
        rentHandler.cancel(rentId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RolesAllowed({CUSTOMER, ADMIN})
    public BigDecimal calculateCancelPenalty(Rent rent) {
        return rentHandler.calculateCancelPenalty(rent);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @RolesAllowed(ADMIN)
    public List<Rent> listPendingPenalizations() {
        return rentHandler.listPendingPenalizations();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RolesAllowed(ADMIN)
    public List<Rent> listAllRents() {
        return rentHandler.listAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RolesAllowed(ADMIN)
    public List<Rent> listFutureRents() {
        return rentHandler.listFutureRents();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @RolesAllowed(ADMIN)
    public void closePenalization(String rentId) throws RentPenalizationException {
        rentHandler.closePenalization(rentId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RolesAllowed({CUSTOMER, ADMIN})
    public Rent getRent(String rentId) {
        return rentHandler.getRent(rentId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PermitAll
    @Transactional
    public void confirmPayment(String rentId) {
        rentHandler.confirmPayment(rentId);
    }
}
