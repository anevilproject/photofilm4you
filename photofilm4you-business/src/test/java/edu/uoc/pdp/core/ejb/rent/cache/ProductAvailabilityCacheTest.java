package edu.uoc.pdp.core.ejb.rent.cache;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import edu.uoc.pdp.core.dao.CategoryDAO;
import edu.uoc.pdp.core.dao.ItemDAO;
import edu.uoc.pdp.core.dao.ProductDAO;
import edu.uoc.pdp.core.dao.ReservationDAO;
import edu.uoc.pdp.core.model.availability.IndexedProduct;
import edu.uoc.pdp.core.model.event.ItemEvent;
import edu.uoc.pdp.db.entity.Category;
import edu.uoc.pdp.db.entity.Item;
import edu.uoc.pdp.db.entity.ItemStatus;
import edu.uoc.pdp.db.entity.Model;
import edu.uoc.pdp.db.entity.Product;
import edu.uoc.pdp.db.entity.Reservation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductAvailabilityCacheTest {

    @Mock
    private ProductDAO productDAO;
    @Mock
    private CategoryDAO categoryDAO;
    @Mock
    private ItemDAO itemDAO;
    @Mock
    private ReservationDAO reservationDAO;

    @InjectMocks
    private ProductAvailabilityCache availabilityCache;


    @Test
    public void mustHaveNoAvailabilityIfNotLoaded() {
        IndexedProduct product = availabilityCache.getAvailableProduct(
                LocalDate.now(), LocalDate.now().plusDays(364), "PRODUCT_ID");

        assertEmpty(product);
    }

    @Test
    public void mustHaveAvailabilityForAllYearWhenProductIsNotRented() {
        mockProduct();
        availabilityCache.load();

        IndexedProduct product = availabilityCache.getAvailableProduct(
                LocalDate.now(), LocalDate.now().plusDays(364), "PRODUCT_ID");

        assertHasOne(product);
    }

    @Test
    public void mustUpdateAvailabilityWhenItemIsRemoved() {
        mockProduct();

        availabilityCache.load();
        availabilityCache.itemRemoved(new ItemEvent(getProduct()));

        IndexedProduct product = availabilityCache.getAvailableProduct(
                LocalDate.now(), LocalDate.now().plusDays(364), "PRODUCT_ID");

        assertEmpty(product);
    }

    @Test
    public void mustUpdateAvailabilityWhenItemIsCreated() {
        mockProduct();

        availabilityCache.load();
        availabilityCache.itemCreated(new ItemEvent(getProduct()));

        IndexedProduct product = availabilityCache.getAvailableProduct(
                LocalDate.now(), LocalDate.now().plusDays(364), "PRODUCT_ID");

        assertNotNull(product);
        assertEquals(2, product.getUnits().get());
    }

    @Test
    public void mustAccountForReservationWhenLoadingAvailability() {
        mockProduct();

        when(reservationDAO.findReservations(eq(Sets.newHashSet("ITEM_ID")), any(), any()))
                .thenReturn(Lists.newArrayList(getReservation()));

        availabilityCache.load();

        IndexedProduct product = availabilityCache.getAvailableProduct(
                LocalDate.now(), LocalDate.now().plusDays(364), "PRODUCT_ID");

        assertEmpty(product);

        product = availabilityCache.getAvailableProduct(
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(364), "PRODUCT_ID");

        assertHasOne(product);
    }

    @Test
    public void shiftRemovesObsoleteAvailability() {
        LocalDate from = LocalDate.now().minusDays(2);
        LocalDate to = LocalDate.now().minusDays(1);

        mockProduct();
        availabilityCache.load();
        availabilityCache.itemCreated(new ItemEvent("PRODUCT_ID", from, to, 1));

        IndexedProduct product = availabilityCache.getAvailableProduct(from, to, "PRODUCT_ID");

        assertHasOne(product);

        availabilityCache.shift();

        product = availabilityCache.getAvailableProduct(from, to, "PRODUCT_ID");

        assertEmpty(product);
    }

    @Test(expected = RejectedExecutionException.class)
    public void destroysSchedulerWhenDestroyed() {
        availabilityCache.destroy();
        availabilityCache.load();
    }

    private void assertEmpty(IndexedProduct product) {
        assertNotNull(product);
        assertEquals(0, product.getUnits().get());
    }

    private void assertHasOne(IndexedProduct product) {
        assertNotNull(product);
        assertEquals(1, product.getUnits().get());
    }

    private void mockProduct() {
        when(productDAO.getActiveProducts()).thenReturn(Lists.newArrayList(getProduct()));
        when(itemDAO.getAvailableItems("PRODUCT_ID")).thenReturn(Lists.newArrayList(getItem()));
    }

    private Item getItem() {
        Item item = new Item();
        item.setId("ITEM_ID");
        item.setProduct(getProduct());
        item.setSerialNumber(UUID.randomUUID().toString());
        item.setStatus(ItemStatus.OPERATIONAL);

        return item;
    }

    private Product getProduct() {
        Product product = new Product();
        product.setId("PRODUCT_ID");
        product.setName("Camera de video");
        product.setCategory(getCategory());
        product.setModel(new Model());
        product.setDescription("Product description");
        product.setDailyPrice(new BigDecimal("225.000"));
        product.setRating(4.5f);

        return product;
    }

    private Category getCategory() {
        Category category = new Category();
        category.setId("CATEGORY_ID");
        category.setName("VIDEO");

        return category;
    }

    private Reservation getReservation() {
        Reservation reservation = new Reservation();
        reservation.setId("RESERVATION_ID");
        reservation.setDate(LocalDate.now());
        reservation.setItem(getItem());

        return reservation;
    }
}
