package edu.uoc.pdp.core.ejb.rent.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
import edu.uoc.pdp.db.entity.Customer;
import edu.uoc.pdp.db.entity.Item;
import edu.uoc.pdp.db.entity.ItemStatus;
import edu.uoc.pdp.db.entity.Product;
import edu.uoc.pdp.db.entity.Rent;
import edu.uoc.pdp.db.entity.RentStatus;
import edu.uoc.pdp.db.entity.Reservation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ShoppingCartHandlerTest {

    @Mock
    private ProductAvailabilityCache availabilityCache;
    @Mock
    private ProductDAO productDAO;
    @Mock
    private ItemDAO itemDAO;
    @Mock
    private ReservationDAO reservationDAO;
    @Mock
    private CustomerDAO customerDAO;
    @Mock
    private SessionManager sessionManager;

    @InjectMocks
    private ShoppingCartHandler cartHandler;


    @Test
    public void getCartIfPresentReturnsSessionCart() {
        LocalDate from = LocalDate.of(2020, 12, 2);
        LocalDate to = LocalDate.of(2020, 12, 4);
        ShoppingCart cart = new ShoppingCart(from, to);

        when(sessionManager.getAttribute("cart", ShoppingCart.class)).thenReturn(cart);

        Optional<ShoppingCart> result = cartHandler.getCartIfPresent();

        assertTrue(result.isPresent());
        assertEquals(cart, result.get());
    }

    @Test
    public void getCartIfPresentReturnsEmptyWhenThereIsNoSessionCart() {
        Optional<ShoppingCart> result = cartHandler.getCartIfPresent();

        assertFalse(result.isPresent());
    }

    @Test
    public void createCartMustSetSessionCart() {
        LocalDate from = LocalDate.of(2020, 12, 2);
        LocalDate to = LocalDate.of(2020, 12, 4);
        ShoppingCart expectedCart = new ShoppingCart(from, to);

        ShoppingCart cart = cartHandler.createCart(from, to);

        verify(sessionManager, times(1)).setAttribute("cart", expectedCart);
        assertEquals(expectedCart, cart);
    }

    @Test(expected = NoShoppingCartException.class)
    public void addToCartThrowsExceptionIfSessionHasNoCart() throws NoShoppingCartException, ProductNotAvailableException {
        cartHandler.addToCart("PRODUCT1", 1);
    }

    @Test(expected = ProductNotAvailableException.class)
    public void addToCartThrowsExceptionIfCacheHasNoAvailability() throws NoShoppingCartException, ProductNotAvailableException {
        LocalDate from = LocalDate.of(2020, 12, 2);
        LocalDate to = LocalDate.of(2020, 12, 4);
        ShoppingCart cart = new ShoppingCart(from, to);

        when(sessionManager.getAttribute("cart", ShoppingCart.class)).thenReturn(cart);
        when(availabilityCache.getAvailableProduct(any(), any(), anyString())).thenReturn(new IndexedProduct("PRODUCT1"));

        cartHandler.addToCart("PRODUCT1", 1);
    }

    @Test(expected = ProductNotAvailableException.class)
    public void addToCartThrowsExceptionIfCacheHasNotEnoughUnits() throws NoShoppingCartException, ProductNotAvailableException {
        LocalDate from = LocalDate.of(2020, 12, 2);
        LocalDate to = LocalDate.of(2020, 12, 4);
        ShoppingCart cart = new ShoppingCart(from, to);

        when(sessionManager.getAttribute("cart", ShoppingCart.class)).thenReturn(cart);
        when(availabilityCache.getAvailableProduct(any(), any(), anyString())).thenReturn(getIndexedProduct(1));

        cartHandler.addToCart("PRODUCT1", 3);
    }

    @Test
    public void addToCartMustAddSpecifiedProductToSessionCart() throws NoShoppingCartException, ProductNotAvailableException {
        LocalDate from = LocalDate.of(2020, 12, 2);
        LocalDate to = LocalDate.of(2020, 12, 4);
        ShoppingCart cart = new ShoppingCart(from, to);

        when(sessionManager.getAttribute("cart", ShoppingCart.class)).thenReturn(cart);
        when(availabilityCache.getAvailableProduct(from, to, "PRODUCT1")).thenReturn(getIndexedProduct(1));
        when(productDAO.getById("PRODUCT1")).thenReturn(getProduct(1));

        cartHandler.addToCart("PRODUCT1", 1);

        assertEquals(1, cart.getItems().size());
        assertEquals(new AvailableProduct(getProduct(1), 1, 3), cart.getItems().get(0));
    }

    @Test
    public void addToCartMustAccumulateToExistingProduct() throws NoShoppingCartException, ProductNotAvailableException {
        LocalDate from = LocalDate.of(2020, 12, 2);
        LocalDate to = LocalDate.of(2020, 12, 4);
        ShoppingCart cart = new ShoppingCart(from, to);

        when(sessionManager.getAttribute("cart", ShoppingCart.class)).thenReturn(cart);
        when(availabilityCache.getAvailableProduct(from, to, "PRODUCT1")).thenReturn(getIndexedProduct(1));
        when(productDAO.getById("PRODUCT1")).thenReturn(getProduct(1));

        cartHandler.addToCart("PRODUCT1", 1);
        cartHandler.addToCart("PRODUCT1", 1);

        assertEquals(1, cart.getItems().size());
        assertEquals(new AvailableProduct(getProduct(1), 2, 3), cart.getItems().get(0));
    }

    @Test(expected = ProductNotAvailableException.class)
    public void addToCartMustThrowExceptionWhenAccumulatedUnitsAreNotAvailable() throws NoShoppingCartException, ProductNotAvailableException {
        LocalDate from = LocalDate.of(2020, 12, 2);
        LocalDate to = LocalDate.of(2020, 12, 4);
        ShoppingCart cart = new ShoppingCart(from, to);

        when(sessionManager.getAttribute("cart", ShoppingCart.class)).thenReturn(cart);
        when(availabilityCache.getAvailableProduct(from, to, "PRODUCT1")).thenReturn(getIndexedProduct(1));
        when(productDAO.getById("PRODUCT1")).thenReturn(getProduct(1));

        cartHandler.addToCart("PRODUCT1", 1);
        cartHandler.addToCart("PRODUCT1", 1);
        cartHandler.addToCart("PRODUCT1", 1);
    }

    @Test
    public void addToCartMustAddMultipleProductsWhenRequested() throws NoShoppingCartException, ProductNotAvailableException {
        LocalDate from = LocalDate.of(2020, 12, 2);
        LocalDate to = LocalDate.of(2020, 12, 4);
        ShoppingCart cart = new ShoppingCart(from, to);

        when(sessionManager.getAttribute("cart", ShoppingCart.class)).thenReturn(cart);
        when(availabilityCache.getAvailableProduct(from, to, "PRODUCT1")).thenReturn(getIndexedProduct(1));
        when(availabilityCache.getAvailableProduct(from, to, "PRODUCT2")).thenReturn(getIndexedProduct(2));
        when(productDAO.getById("PRODUCT1")).thenReturn(getProduct(1));
        when(productDAO.getById("PRODUCT2")).thenReturn(getProduct(2));

        cartHandler.addToCart("PRODUCT1", 2);
        cartHandler.addToCart("PRODUCT2", 1);

        assertEquals(2, cart.getItems().size());
        assertEquals(2, cart.getUnitsOfProduct("PRODUCT1"));
        assertEquals(1, cart.getUnitsOfProduct("PRODUCT2"));
        assertEquals(BigDecimal.valueOf(46125, 2), cart.getTotalPrice());
    }

    @Test
    public void removeFromCartMustRemoveProduct() throws NoShoppingCartException, ProductNotAvailableException {
        LocalDate from = LocalDate.of(2020, 12, 2);
        LocalDate to = LocalDate.of(2020, 12, 4);
        ShoppingCart cart = new ShoppingCart(from, to);

        when(sessionManager.getAttribute("cart", ShoppingCart.class)).thenReturn(cart);
        when(availabilityCache.getAvailableProduct(from, to, "PRODUCT1")).thenReturn(getIndexedProduct(1));
        when(productDAO.getById("PRODUCT1")).thenReturn(getProduct(1));

        cartHandler.addToCart("PRODUCT1", 2);

        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getUnitsOfProduct("PRODUCT1"));

        cartHandler.removeFromCart("PRODUCT1");

        assertTrue(cart.getItems().isEmpty());
        assertEquals(0, cart.getUnitsOfProduct("PRODUCT1"));
    }

    @Test(expected = NoShoppingCartException.class)
    public void mapCartToRentMustThrowExceptionWhenThereIsNoCart() throws NoShoppingCartException, ProductNotAvailableException {
        cartHandler.mapCartToRent();
    }

    @Test(expected = NoShoppingCartException.class)
    public void mapCartToRentMustThrowExceptionWhenCartIsEmpty() throws NoShoppingCartException, ProductNotAvailableException {
        LocalDate from = LocalDate.of(2020, 12, 2);
        LocalDate to = LocalDate.of(2020, 12, 4);

        when(sessionManager.getAttribute("cart", ShoppingCart.class)).thenReturn(new ShoppingCart(from, to));

        cartHandler.mapCartToRent();
    }

    @Test
    public void mapCartToRentMustCreateUnconfirmedRent() throws NoShoppingCartException, ProductNotAvailableException {
        LocalDate from = LocalDate.of(2020, 12, 2);
        LocalDate to = LocalDate.of(2020, 12, 4);
        ShoppingCart cart = new ShoppingCart(from, to);
        cart.addItem(getProduct(1), 2);
        cart.addItem(getProduct(2), 2);

        when(sessionManager.getAttribute("cart", ShoppingCart.class)).thenReturn(cart);
        when(availabilityCache.getAvailableProduct(from, to, "PRODUCT1")).thenReturn(getIndexedProduct(1));
        when(availabilityCache.getAvailableProduct(from, to, "PRODUCT2")).thenReturn(getIndexedProduct(2));
        when(sessionManager.getUserId()).thenReturn("email@email.com");
        when(customerDAO.getByEmail("email@email.com")).thenReturn(getCustomer());
        when(itemDAO.getAvailableItems("PRODUCT1")).thenReturn(Lists.newArrayList(
                getItem(1, 1), getItem(2, 1)));
        when(itemDAO.getAvailableItems("PRODUCT2")).thenReturn(Lists.newArrayList(
                getItem(3, 2), getItem(4, 2)));
        when(reservationDAO.findReservations(Sets.newHashSet("ITEM1", "ITEM2"), from, to))
                .thenReturn(Collections.emptyList());
        when(reservationDAO.findReservations(Sets.newHashSet("ITEM3", "ITEM4"), from, to))
                .thenReturn(Collections.emptyList());

        Rent rent = cartHandler.mapCartToRent();

        List<Item> expectedItems = Lists.newArrayList(
                getItem(1, 1),
                getItem(2, 1),
                getItem(3, 2),
                getItem(4, 2));

        assertNotNull(rent);
        assertEquals(LocalDate.now(), rent.getCreated().toLocalDate());
        assertNull(rent.getId());
        assertEquals(getCustomer(), rent.getCustomer());
        assertEquals(RentStatus.NOT_CONFIRMED, rent.getStatus());
        assertEquals(from, rent.getFrom());
        assertEquals(to, rent.getTo());
        assertNull(rent.getCancellation());
        assertEquals(BigDecimal.valueOf(61500, 2), rent.getTotalPrice());
        assertTrue(rent.getItems().containsAll(expectedItems));
    }

    @Test(expected = ProductNotAvailableException.class)
    public void mapCartToRentMustThrowExceptionWhenThereAreNoAvailableItems() throws NoShoppingCartException, ProductNotAvailableException {
        LocalDate from = LocalDate.of(2020, 12, 2);
        LocalDate to = LocalDate.of(2020, 12, 4);
        ShoppingCart cart = new ShoppingCart(from, to);
        cart.addItem(getProduct(1), 2);

        when(sessionManager.getAttribute("cart", ShoppingCart.class)).thenReturn(cart);
        when(availabilityCache.getAvailableProduct(from, to, "PRODUCT1")).thenReturn(getIndexedProduct(1));
        when(itemDAO.getAvailableItems("PRODUCT1")).thenReturn(Collections.emptyList());

        cartHandler.mapCartToRent();
    }

    @Test(expected = ProductNotAvailableException.class)
    public void mapCartToRentMustThrowExceptionWhenThereIsNoCacheAvailability() throws NoShoppingCartException, ProductNotAvailableException {
        LocalDate from = LocalDate.of(2020, 12, 2);
        LocalDate to = LocalDate.of(2020, 12, 4);
        ShoppingCart cart = new ShoppingCart(from, to);
        cart.addItem(getProduct(1), 2);

        when(sessionManager.getAttribute("cart", ShoppingCart.class)).thenReturn(cart);
        when(availabilityCache.getAvailableProduct(from, to, "PRODUCT1")).thenReturn(new IndexedProduct("PRODUCT1"));

        cartHandler.mapCartToRent();
    }

    @Test(expected = ProductNotAvailableException.class)
    public void mapCartToRentMustThrowExceptionWhenThereIsLessStockThanRequested() throws NoShoppingCartException, ProductNotAvailableException {
        LocalDate from = LocalDate.of(2020, 12, 2);
        LocalDate to = LocalDate.of(2020, 12, 4);
        ShoppingCart cart = new ShoppingCart(from, to);
        cart.addItem(getProduct(1), 2);

        when(sessionManager.getAttribute("cart", ShoppingCart.class)).thenReturn(cart);
        when(availabilityCache.getAvailableProduct(from, to, "PRODUCT1")).thenReturn(getIndexedProduct(1));
        when(itemDAO.getAvailableItems("PRODUCT1")).thenReturn(Lists.newArrayList(
                getItem(1, 1), getItem(2, 1)));
        when(reservationDAO.findReservations(Sets.newHashSet("ITEM1", "ITEM2"), from, to))
                .thenReturn(Lists.newArrayList(getReservation()));

        try {
            cartHandler.mapCartToRent();
        } catch (ProductNotAvailableException e) {
            assertTrue(cart.getItems().isEmpty());

            throw e;
        }
    }

    @Test
    public void deleteCartSetsNullSessionAttribute() {
        cartHandler.deleteCart();

        verify(sessionManager, times(1)).setAttribute("cart", null);
    }

    private IndexedProduct getIndexedProduct(int index) {
        IndexedProduct product = new IndexedProduct("PRODUCT" + index);
        product.getUnits().addAndGet(2);

        return product;
    }

    private Product getProduct(int index) {
        Product product = new Product();
        product.setId("PRODUCT" + index);
        product.setDailyPrice(BigDecimal.valueOf(51.25));

        return product;
    }

    private Item getItem(int itemNum, int productNum) {
        Item item = new Item();
        item.setId("ITEM" + itemNum);
        item.setProduct(getProduct(productNum));
        item.setSerialNumber(UUID.randomUUID().toString());
        item.setStatus(ItemStatus.OPERATIONAL);

        return item;
    }

    private Customer getCustomer() {
        Customer customer = new Customer();
        customer.setEmail("email@email.com");

        return customer;
    }

    private Reservation getReservation() {
        Reservation reservation = new Reservation();
        reservation.setId("RESERVATION_ID");
        reservation.setDate(LocalDate.now());
        reservation.setItem(getItem(1, 1));

        return reservation;
    }
}
