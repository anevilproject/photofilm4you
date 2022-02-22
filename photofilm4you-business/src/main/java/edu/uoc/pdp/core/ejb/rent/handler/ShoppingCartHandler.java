package edu.uoc.pdp.core.ejb.rent.handler;

import edu.uoc.pdp.core.dao.CustomerDAO;
import edu.uoc.pdp.core.dao.ItemDAO;
import edu.uoc.pdp.core.dao.ProductDAO;
import edu.uoc.pdp.core.dao.ReservationDAO;
import edu.uoc.pdp.core.ejb.rent.cache.ProductAvailabilityCache;
import edu.uoc.pdp.core.ejb.session.SessionManager;
import edu.uoc.pdp.core.exception.NoShoppingCartException;
import edu.uoc.pdp.core.exception.ProductNotAvailableException;
import edu.uoc.pdp.core.model.availability.AvailableProduct;
import edu.uoc.pdp.core.model.availability.IndexedProduct;
import edu.uoc.pdp.core.model.cart.ShoppingCart;
import edu.uoc.pdp.db.entity.Item;
import edu.uoc.pdp.db.entity.Rent;
import edu.uoc.pdp.db.entity.RentStatus;
import edu.uoc.pdp.db.entity.Reservation;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class ShoppingCartHandler {

    private static final String CART = "cart";

    @Inject
    private ProductAvailabilityCache availabilityCache;
    @Inject
    private ProductDAO productDAO;
    @Inject
    private ItemDAO itemDAO;
    @Inject
    private ReservationDAO reservationDAO;
    @Inject
    private CustomerDAO customerDAO;
    @Inject
    private SessionManager sessionManager;


    /**
     * Returns the current shopping cart in the session if any
     *
     * @return An optional containing the current cart, empty if there is no cart in the session
     */
    public Optional<ShoppingCart> getCartIfPresent() {
        return Optional.ofNullable(sessionManager.getAttribute(CART, ShoppingCart.class));
    }

    /**
     * Creates a new shopping cart associated to the current http session. Any previously existing cart will be overridden
     *
     * @param from Date from
     * @param to   Date to
     * @return A new shopping cart instance
     */
    public ShoppingCart createCart(LocalDate from, LocalDate to) {
        ShoppingCart cart = new ShoppingCart(from, to);
        sessionManager.setAttribute(CART, cart);

        return cart;
    }

    /**
     * Deletes the current session cart
     */
    public void deleteCart() {
        sessionManager.setAttribute(CART, null);
    }

    /**
     * Adds the specified amount of units of a product to the cart
     *
     * @param product Product to be added
     * @param units   Number of units to be added
     * @throws NoShoppingCartException      If there is no shopping cart
     * @throws ProductNotAvailableException If the requested product is not available in the cart dates
     */
    public void addToCart(String product, int units) throws NoShoppingCartException, ProductNotAvailableException {
        ShoppingCart cart = getCart();

        checkCacheAvailability(cart, product, units + cart.getUnitsOfProduct(product), false);

        cart.addItem(productDAO.getById(product), units);
    }

    /**
     * Removes a product from the current cart
     *
     * @param product Id of the product to be removed
     */
    public void removeFromCart(String product) throws NoShoppingCartException {
        getCart().removeProduct(product);
    }

    /**
     * Transforms the current shopping cart instance into a Rent object.
     * <p>
     * This method will automatically and randomly map concrete item units of each product in the cart and will check its
     * availability between the requested dates both with the product cache and database.
     *
     * @return A rent object with the products specified in the current cart
     * @throws NoShoppingCartException      If the cart is empty or has no products
     * @throws ProductNotAvailableException If any of the specified products has not enough units to cover the requested cart
     */
    public Rent mapCartToRent() throws NoShoppingCartException, ProductNotAvailableException {
        ShoppingCart cart = getCart();

        checkCartHasItems(cart);
        checkCacheAvailability(cart);

        Rent rent = new Rent();
        rent.setCreated(LocalDateTime.now());
        rent.setFrom(cart.getFrom());
        rent.setTo(cart.getTo());
        rent.setStatus(RentStatus.NOT_CONFIRMED);
        rent.setTotalPrice(cart.getTotalPrice());
        rent.setItems(assignItems(cart));
        rent.setCustomer(customerDAO.getByEmail(sessionManager.getUserId()));

        return rent;
    }

    private ShoppingCart getCart() throws NoShoppingCartException {
        return getCartIfPresent().orElseThrow(NoShoppingCartException::new);
    }

    private List<Item> assignItems(ShoppingCart cart) throws ProductNotAvailableException {
        List<Item> items = new ArrayList<>();

        for (AvailableProduct product : cart.getItems()) {
            items.addAll(getAvailableItems(cart, product));
        }
        return items;
    }

    private List<Item> getAvailableItems(ShoppingCart cart, AvailableProduct product) throws ProductNotAvailableException {
        Map<String, Item> items = itemDAO.getAvailableItems(product.getId()).stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));

        reservationDAO.findReservations(items.keySet(), cart.getFrom(), cart.getTo()).stream()
                .map(Reservation::getItemId)
                .forEach(items::remove);

        if (items.size() < product.getUnits()) {
            cart.removeProduct(product.getId());

            throw new ProductNotAvailableException(product.getId());
        }
        return items.values().stream().limit(product.getUnits()).collect(Collectors.toList());
    }

    private void checkCartHasItems(ShoppingCart cart) throws NoShoppingCartException {
        if (cart.getItems().isEmpty()) {
            throw new NoShoppingCartException();
        }
    }

    private void checkCacheAvailability(ShoppingCart cart) throws ProductNotAvailableException {
        for (AvailableProduct product : cart.getItems()) {
            checkCacheAvailability(cart, product.getId(), product.getUnits(), true);
        }
    }

    private void checkCacheAvailability(
            ShoppingCart cart,
            String product,
            int units,
            boolean removeFromCart) throws ProductNotAvailableException {
        IndexedProduct availability = availabilityCache.getAvailableProduct(cart.getFrom(), cart.getTo(), product);

        if (availability.getUnits().get() < units) {
            if (removeFromCart) {
                cart.removeProduct(product);
            }
            throw new ProductNotAvailableException(product);
        }
    }
}
