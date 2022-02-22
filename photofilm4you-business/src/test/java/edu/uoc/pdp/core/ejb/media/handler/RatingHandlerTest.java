package edu.uoc.pdp.core.ejb.media.handler;

import edu.uoc.pdp.core.dao.CustomerDAO;
import edu.uoc.pdp.core.dao.ProductDAO;
import edu.uoc.pdp.core.dao.ProductRatingDAO;
import edu.uoc.pdp.core.ejb.session.SessionManager;
import edu.uoc.pdp.db.entity.Customer;
import edu.uoc.pdp.db.entity.Product;
import edu.uoc.pdp.db.entity.ProductRating;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RatingHandlerTest {

    @Mock
    private ProductRatingDAO productRatingDAO;
    @Mock
    private ProductDAO productDAO;
    @Mock
    private CustomerDAO customerDAO;
    @Mock
    private SessionManager sessionManager;

    @InjectMocks
    private RatingHandler ratingHandler;


    @Test
    public void rateProductMustSaveAndUpdateRating() {
        when(productDAO.getById("PRODUCT_ID")).thenReturn(getProduct());
        when(sessionManager.getUserId()).thenReturn("customer");
        when(customerDAO.getByEmail("customer")).thenReturn(getCustomer());

        ratingHandler.rateProduct("PRODUCT_ID", 5, "A comment");

        ArgumentCaptor<ProductRating> ratingCaptor = ArgumentCaptor.forClass(ProductRating.class);
        verify(productRatingDAO, times(1)).save(ratingCaptor.capture());

        ProductRating rating = ratingCaptor.getValue();
        assertNotNull(rating);
        assertEquals("A comment", rating.getComment());
        assertEquals(5, rating.getRating());
        assertNotNull(rating.getCreated());
        assertEquals(getCustomer(), rating.getCustomer());
        assertEquals(getProduct(), rating.getProduct());

        verify(productDAO, times(1)).updateRating("PRODUCT_ID");
    }

    private Customer getCustomer() {
        Customer customer = new Customer();
        customer.setId("customer_id");

        return customer;
    }

    private Product getProduct() {
        Product product = new Product();
        product.setId("PRODUCT_ID");

        return product;
    }
}
