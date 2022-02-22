package edu.uoc.pdp.core.ejb.catalog.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import edu.uoc.pdp.core.dao.CategoryDAO;
import edu.uoc.pdp.core.dao.ProductDAO;
import edu.uoc.pdp.core.ejb.media.handler.ImageHandler;
import edu.uoc.pdp.core.ejb.rent.cache.ProductCategoryCache;
import edu.uoc.pdp.core.exception.ImageException;
import edu.uoc.pdp.core.exception.ProductException;
import edu.uoc.pdp.core.model.availability.ProductQuery;
import edu.uoc.pdp.core.model.file.UploadedFile;
import edu.uoc.pdp.db.entity.Category;
import edu.uoc.pdp.db.entity.Image;
import edu.uoc.pdp.db.entity.Model;
import edu.uoc.pdp.db.entity.Product;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductHandlerTest {

    @Mock
    private ProductDAO productDAO;
    @Mock
    private CategoryDAO categoryDAO;
    @Mock
    private ImageHandler imageHandler;

    @InjectMocks
    private ProductCategoryCache categoryCache;
    private ProductHandler productHandler;

    @Before
    public void setup() {
        productHandler = new ProductHandler();
        productHandler.setCategoryCache(categoryCache);
        productHandler.setProductDAO(productDAO);
        productHandler.setImageHandler(imageHandler);

        when(productDAO.getAll()).thenReturn(getAllProducts());
        when(categoryDAO.getById("CATEGORY_ID")).thenReturn(getCategory());

        categoryCache.load();
    }

    @Test
    public void findActiveProductsMustReturnOnlyActiveProducts() {
        when(productDAO.findProducts(Sets.newHashSet("PRODUCT_ID1"), null, null))
                .thenReturn(Lists.newArrayList(getProduct(null, 1)));

        List<Product> result = productHandler.findActiveProducts(new ProductQuery(null));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getDeleted());
    }

    @Test
    public void findActiveProductsMustFilterByBrandAndModel() {
        ProductQuery query = new ProductQuery("CATEGORY_ID");
        query.setBrand("BRAND");
        query.setModel("MODEL");

        when(productDAO.findProducts(Sets.newHashSet("PRODUCT_ID1"), "BRAND", "MODEL"))
                .thenReturn(Lists.newArrayList(getProduct(null, 1)));

        List<Product> result = productHandler.findActiveProducts(query);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getDeleted());
    }

    @Test
    public void findActiveProductsMustFilterByName() {
        ProductQuery query = new ProductQuery("CATEGORY_ID");
        query.setBrand("BRAND");
        query.setModel("MODEL");
        query.setName("test");

        when(productDAO.findProducts(Sets.newHashSet("PRODUCT_ID1"), "BRAND", "MODEL"))
                .thenReturn(Lists.newArrayList(getProduct(null, 1)));

        List<Product> result = productHandler.findActiveProducts(query);

        assertTrue(result.isEmpty());
    }

    @Test
    public void findActiveProductsMustReturnProductsMatchingByName() {
        ProductQuery query = new ProductQuery("CATEGORY_ID");
        query.setBrand("BRAND");
        query.setModel("MODEL");
        query.setName("video camera");

        when(productDAO.findProducts(Sets.newHashSet("PRODUCT_ID1"), "BRAND", "MODEL"))
                .thenReturn(Lists.newArrayList(getProduct(null, 1)));

        List<Product> result = productHandler.findActiveProducts(query);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getDeleted());
    }

    @Test
    public void saveMustUpdateImageIfPresent() throws ImageException, ProductException {
        Product product = getProduct(null, 1);
        Image old = product.getImage();
        when(imageHandler.processImage(any())).thenReturn(new Image());

        productHandler.save(product, getImageFile());

        assertNotSame(old, product.getImage());
        verify(productDAO, times(1)).save(product);
        verify(imageHandler, times(1)).deleteImage(old);
    }

    @Test
    public void saveMustNotChangeImageIfNotPresent() throws ImageException, ProductException {
        Product product = getProduct(null, 1);
        Image old = product.getImage();

        productHandler.save(product, null);

        assertSame(old, product.getImage());
        verify(productDAO, times(1)).save(product);
        verify(imageHandler, times(1)).deleteImage(null);
    }

    @Test(expected = ProductException.class)
    public void saveMustThrowExceptionIfProductExists() throws ImageException, ProductException {
        Product product = getProduct(null, 1);

        when(productDAO.existsProduct(product)).thenReturn(true);

        productHandler.save(product, null);
    }

    private UploadedFile getImageFile() {
        return new UploadedFile("image/png", "test.png", new byte[0]);
    }

    private List<Product> getAllProducts() {
        return Lists.newArrayList(getProduct(null, 1), getProduct(LocalDateTime.now(), 2));
    }

    private Product getProduct(LocalDateTime deleted, int num) {
        Product product = new Product();
        product.setId("PRODUCT_ID" + num);
        product.setName("Camera de video");
        product.setCategory(getCategory());
        product.setModel(new Model());
        product.setDescription("Product description");
        product.setDailyPrice(new BigDecimal("225.000"));
        product.setRating(4.5f);
        product.setDeleted(deleted);
        product.setImage(new Image());

        return product;
    }

    private Category getCategory() {
        Category category = new Category();
        category.setId("CATEGORY_ID");
        category.setName("VIDEO");

        return category;
    }

}
