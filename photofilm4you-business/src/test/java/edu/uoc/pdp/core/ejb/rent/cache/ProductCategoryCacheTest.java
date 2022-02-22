package edu.uoc.pdp.core.ejb.rent.cache;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import edu.uoc.pdp.core.dao.CategoryDAO;
import edu.uoc.pdp.core.dao.ProductDAO;
import edu.uoc.pdp.db.entity.Category;
import edu.uoc.pdp.db.entity.Model;
import edu.uoc.pdp.db.entity.Product;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductCategoryCacheTest {

    @Mock
    private ProductDAO productDAO;
    @Mock
    private CategoryDAO categoryDAO;

    @InjectMocks
    private ProductCategoryCache categoryCache;


    @Test
    public void mustIndexProductsByCategory() {
        when(categoryDAO.getById("CATEGORY_ID")).thenReturn(getCategory());
        when(productDAO.getAll()).thenReturn(Lists.newArrayList(getProduct()));

        categoryCache.load();

        Set<String> products = categoryCache.getActiveCategoryProducts("CATEGORY_ID");

        assertEquals(Sets.newHashSet("PRODUCT_ID"), products);
    }

    @Test
    public void mustIndexDisabledProductsByCategory() {
        when(categoryDAO.getById("CATEGORY_ID")).thenReturn(getCategory());
        when(productDAO.getAll()).thenReturn(Lists.newArrayList(getProduct(), getProductDisabled()));

        categoryCache.load();

        Set<String> products = categoryCache.getActiveCategoryProducts("CATEGORY_ID");

        assertEquals(Sets.newHashSet("PRODUCT_ID"), products);

        Set<String> allProducts = categoryCache.getCategoryProducts("CATEGORY_ID");

        assertEquals(Sets.newHashSet("PRODUCT_ID2", "PRODUCT_ID"), allProducts);
    }

    private Product getProductDisabled() {
        Product product = new Product();
        product.setId("PRODUCT_ID2");
        product.setName("Camera de video");
        product.setCategory(getCategory());
        product.setModel(new Model());
        product.setDescription("Product description");
        product.setDailyPrice(new BigDecimal("225.000"));
        product.setRating(4.5f);
        product.setDeleted(LocalDateTime.now());

        return product;
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
}
