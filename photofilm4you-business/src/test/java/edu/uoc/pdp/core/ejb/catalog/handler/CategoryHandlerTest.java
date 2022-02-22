package edu.uoc.pdp.core.ejb.catalog.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import edu.uoc.pdp.core.configuration.ConfigurationProperties;
import edu.uoc.pdp.core.dao.CategoryDAO;
import edu.uoc.pdp.core.ejb.rent.cache.ProductCategoryCache;
import edu.uoc.pdp.core.exception.CategoryException;
import edu.uoc.pdp.core.model.catalog.CategoryTree;
import edu.uoc.pdp.db.entity.Category;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CategoryHandlerTest {

    @Mock
    private CategoryDAO categoryDAO;
    @Mock
    private ProductCategoryCache categoryCache;
    @Mock
    private ConfigurationProperties properties;

    @InjectMocks
    private CategoryHandler categoryHandler;


    @Test
    public void mustBuildCategoryTree() {
        when(categoryDAO.getAll()).thenReturn(getCategories());
        when(categoryCache.getActiveCategoryProducts(anyString())).thenReturn(Sets.newHashSet("product"));

        CategoryTree tree = categoryHandler.getActiveCategoryTree();

        assertEquals(3, tree.getDepth("category_4"));
        assertEquals(1, tree.getDepth("category_2"));
        assertFalse(tree.isCyclic());
        assertEquals("Category name 1 / Category name 3 / Category name 4", tree.resolveNamePath("category_4"));
        assertEquals("Category name 2", tree.resolveNamePath("category_2"));
    }

    @Test
    public void resolveNamePathMustReturnEmptyWhenNoCategory() {
        when(categoryDAO.getAll()).thenReturn(getCategories());
        when(categoryCache.getActiveCategoryProducts(anyString())).thenReturn(Sets.newHashSet("product"));

        CategoryTree tree = categoryHandler.getActiveCategoryTree();
        String path = tree.resolveNamePath("test_test");

        assertEquals("", path);
    }

    @Test
    public void getActiveCategoryTreeMustRemoveCategoriesWithoutProducts() {
        when(categoryDAO.getAll()).thenReturn(getCategories());
        when(categoryCache.getActiveCategoryProducts(anyString())).thenReturn(Sets.newHashSet("product"));
        when(categoryCache.getActiveCategoryProducts("category_4")).thenReturn(Collections.emptySet());

        CategoryTree tree = categoryHandler.getActiveCategoryTree();

        assertEquals("", tree.resolveNamePath("category_4"));
    }

    @Test
    public void treeMustBeMarkedAsCyclicIfTheresADependencyCycle() {
        when(categoryDAO.getAll()).thenReturn(getCategories(true));
        when(categoryCache.getActiveCategoryProducts(anyString())).thenReturn(Sets.newHashSet("product"));

        CategoryTree tree = categoryHandler.getActiveCategoryTree();

        assertTrue(tree.isCyclic());
    }

    @Test(expected = CategoryException.class)
    public void saveMustFailIfCategoriesFormACycle() throws CategoryException {
        when(categoryDAO.getAll()).thenReturn(getCategories());

        Category category = getCategory(1, 4);

        categoryHandler.save(category);
    }


    @Test(expected = CategoryException.class)
    public void saveMustFailIfCategoriesDepthIsGreaterThan3() throws CategoryException {
        when(categoryDAO.getAll()).thenReturn(getCategories());
        when(properties.getMaxCategoryDepth()).thenReturn(3);

        Category category = getCategory(5, 4);

        categoryHandler.save(category);
    }

    @Test(expected = CategoryException.class)
    public void saveMustFailIfCategoriesDepthIsGreaterThan3NewCategory() throws CategoryException {
        when(categoryDAO.getAll()).thenReturn(getCategories());
        when(properties.getMaxCategoryDepth()).thenReturn(3);

        Category category = getCategory(5, 4);
        category.setId(null);

        categoryHandler.save(category);
    }

    @Test
    public void saveMustSaveCategory() throws CategoryException {
        when(categoryDAO.getAll()).thenReturn(getCategories());
        when(properties.getMaxCategoryDepth()).thenReturn(3);

        Category category = getCategory(5, null);

        categoryHandler.save(category);

        verify(categoryDAO, times(1)).save(category);
    }

    @Test(expected = CategoryException.class)
    public void deleteMustFailIfCategoryIsParent() throws CategoryException {
        when(categoryDAO.isParent("categoryId")).thenReturn(true);

        categoryHandler.delete("categoryId");
    }

    @Test(expected = CategoryException.class)
    public void deleteMustFailIfCategoryHasProducts() throws CategoryException {
        when(categoryDAO.isParent("categoryId")).thenReturn(false);
        when(categoryCache.getCategoryProducts("categoryId")).thenReturn(Sets.newHashSet("product"));

        categoryHandler.delete("categoryId");
    }

    @Test
    public void deleteMustDeleteProduct() throws CategoryException {
        when(categoryDAO.isParent("categoryId")).thenReturn(false);
        when(categoryCache.getCategoryProducts("categoryId")).thenReturn(Sets.newHashSet());

        categoryHandler.delete("categoryId");

        verify(categoryDAO, times(1)).delete("categoryId");
    }

    private List<Category> getCategories() {
        return getCategories(false);
    }

    private List<Category> getCategories(boolean cycle) {
        return Lists.newArrayList(
                getCategory(1, cycle ? 4 : null),
                getCategory(2, null),
                getCategory(3, 1),
                getCategory(4, 3));
    }

    private Category getCategory(int num, Integer parent) {
        Category category = new Category();
        category.setId("category_" + num);
        category.setName("category name " + num);

        if (parent != null) {
            category.setParent(getCategory(parent, null));
        }
        return category;
    }
}
