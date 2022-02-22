package edu.uoc.pdp.core.ejb.catalog.handler;

import edu.uoc.pdp.core.configuration.ConfigurationProperties;
import edu.uoc.pdp.core.dao.CategoryDAO;
import edu.uoc.pdp.core.ejb.rent.cache.ProductCategoryCache;
import edu.uoc.pdp.core.exception.CategoryException;
import edu.uoc.pdp.core.model.catalog.CategoryTree;
import edu.uoc.pdp.db.entity.Category;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Singleton
public class CategoryHandler {

    @Inject
    private CategoryDAO categoryDAO;
    @Inject
    private ProductCategoryCache categoryCache;
    @Inject
    private ConfigurationProperties properties;


    /**
     * Retrieves all active categories
     *
     * @return A list with all active categories
     */
    public CategoryTree getActiveCategoryTree() {
        List<Category> categories = categoryDAO.getAll();
        categories.removeIf(this::hasNoProducts);

        return new CategoryTree(categories);
    }

    /**
     * Deletes a category by id
     *
     * @param categoryId category id
     */
    public void delete(String categoryId) throws CategoryException {
        if (categoryDAO.isParent(categoryId)) {
            throw new CategoryException("Hi ha almenys una categoria que té aquesta categoria com a pare.");
        }
        if (!categoryCache.getCategoryProducts(categoryId).isEmpty()) {
            throw new CategoryException("Aquesta categoria te productes associats i no es pot esborrar.");
        }
        categoryDAO.delete(categoryId);
    }

    /**
     * Retrieves a category by id
     *
     * @param categoryId category id
     * @return Matching category if any
     * @throws javax.persistence.EntityNotFoundException if there's no category with the specified id
     */
    public Category getCategory(String categoryId) {
        return categoryDAO.getById(categoryId);
    }

    /**
     * Persists a category instance
     *
     * @param category Category
     * @throws CategoryException If the category tree is in an invalid state after storing the category
     */
    public void save(Category category) throws CategoryException {
        category.setName(StringUtils.upperCase(StringUtils.normalizeSpace(category.getName())).trim());
        checkCategoryTreeConsistency(category);

        categoryDAO.save(category);
    }

    /**
     * Returns all categories
     *
     * @return A list with all categories
     */
    public List<Category> getAll() {
        return categoryDAO.getAll();
    }

    private void checkCategoryTreeConsistency(Category category) throws CategoryException {
        List<Category> categories = categoryDAO.getAll();
        categories.removeIf(cat -> cat.getId().equals(category.getId()));

        if (categories.stream().anyMatch(cat -> matchesName(cat, category))) {
            throw new CategoryException("Ja existeix una categoria amb aquest nom");
        }

        CategoryTree tree = buildCategoryTree(category, categories);

        if (tree.isCyclic()) {
            String cycle = tree.getCycleCategories().stream()
                    .map(Category::getName)
                    .collect(Collectors.joining(", "));

            throw new CategoryException("Hi categories formant un cicle: " + cycle);
        }
        if (tree.getDepth(getCategoryNodeId(category)) > properties.getMaxCategoryDepth()) {
            String name = tree.resolveNamePath(getCategoryNodeId(category));
            throw new CategoryException("No es poden tenir més de 3 categories anidades (" + name + ").");
        }
    }

    private boolean matchesName(Category category1, Category category2) {
        return Objects.equals(getNormalizedName(category1), getNormalizedName(category2));
    }

    private String getNormalizedName(Category category) {
        return StringUtils.lowerCase(StringUtils.stripAccents(category.getName()));
    }

    private CategoryTree buildCategoryTree(Category category, List<Category> categories) {
        Category node = new Category();
        node.setName(category.getName());
        node.setId(getCategoryNodeId(category));
        node.setParent(category.getParent());

        categories.add(node);

        return new CategoryTree(categories);
    }

    private String getCategoryNodeId(Category category) {
        return category.getId() == null ? "new" : category.getId();
    }

    private boolean hasNoProducts(Category category) {
        return categoryCache.getActiveCategoryProducts(category.getId()).isEmpty();
    }
}
