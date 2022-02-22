package edu.uoc.pdp.core.ejb.catalog;

import edu.uoc.pdp.core.exception.BrandException;
import edu.uoc.pdp.core.exception.CategoryException;
import edu.uoc.pdp.core.exception.ImageException;
import edu.uoc.pdp.core.exception.ItemException;
import edu.uoc.pdp.core.exception.ModelException;
import edu.uoc.pdp.core.exception.ProductException;
import edu.uoc.pdp.core.model.availability.ProductQuery;
import edu.uoc.pdp.core.model.catalog.CategoryTree;
import edu.uoc.pdp.core.model.file.UploadedFile;
import edu.uoc.pdp.db.entity.Brand;
import edu.uoc.pdp.db.entity.Category;
import edu.uoc.pdp.db.entity.Item;
import edu.uoc.pdp.db.entity.Model;
import edu.uoc.pdp.db.entity.Product;

import javax.ejb.Local;
import java.util.List;


/**
 * Session EJB Remote Interfaces
 */
@Local
public interface CatalogFacade {

    /**
     * Retrieves a category by id
     *
     * @param categoryId Category id
     * @return Matching category
     */
    Category getCategory(String categoryId);

    /**
     * Deletes a category by id
     *
     * @param categoryId category id
     * @throws CategoryException If the category cannot be deleted
     */
    void deleteCategory(String categoryId) throws CategoryException;

    /**
     * Persists a category instance
     *
     * @param item Category
     * @throws CategoryException If the category tree is in an invalid state with the modification
     */
    void saveCategory(Category item) throws CategoryException;

    /**
     * Retrieves all categories in the catalog
     *
     * @return A List of all categories
     */
    List<Category> listAllCategories();

    /**
     * @param modelId String modelId
     * @return A new model
     */
    Model getModel(String modelId);

    /**
     * Persists a model instance
     *
     * @param model Model
     */
    void saveModel(Model model) throws ModelException;

    /**
     * Removes a model
     */
    void deleteModel(String modelId);

    /**
     * Retrieves all models in the catalog
     *
     * @return A List of all models
     */
    List<Model> listAllModels();

    /**
     * Retrieves a brand by id
     *
     * @param brandId Brand identifier
     * @return Matching brand
     */
    Brand getBrand(String brandId);

    /**
     * Persists a brand instance
     *
     * @param brand Brand
     */
    void saveBrand(Brand brand) throws BrandException;

    /**
     * Removes a brand
     */
    void deleteBrand(String brandId);

    /**
     * Retrieves all brands in the catalog
     *
     * @return A List of all brands
     */
    List<Brand> listAllBrands();

    /**
     * Persists
     *
     * @param product Product
     * @param image   Uploaded image file (optional)
     */
    void saveProduct(Product product, UploadedFile image) throws ImageException, ProductException;

    /**
     * Removes a product
     *
     * @param productId Product identifier
     *                  <p>
     *                  javax.persistence.EntityNotFoundException if there's no Product matching by id
     */
    void deleteProduct(String productId);

    /**
     * Retrieves all products for a given category
     *
     * @return A List of all products
     */
    List<Product> listAllProducts();

    /**
     * Shows details of a product
     *
     * @param productId Product identifier
     * @return A Product
     */
    Product getProduct(String productId);


    /**
     * Retrieves all items
     *
     * @return A List of all items
     */
    List<Item> listAllItems();

    /**
     * Retrieves an item by id
     *
     * @param itemId Item identifier
     * @return Matching item
     */
    Item getItem(String itemId);

    /**
     * Persists an item instance
     *
     * @param item Item
     */
    void saveItem(Item item) throws ItemException;

    /**
     * Removes a product
     *
     * @param itemId Item identifier
     *               <p>
     *               javax.persistence.EntityNotFoundException if there's no Item matching by id
     */
    void deleteItem(String itemId);

    /**
     * Retrieves all items that belong to the selected product
     *
     * @param productName Product identifier
     * @return A List of all items for the specified product
     */
    List<Item> listItemsByProduct(String productName);

    /**
     * Retrieves the number of operational items that belong to the selected product
     *
     * @param productId Product identifier
     * @return Number of items
     */
    long countAvailableItemsByProduct(String productId);

    /**
     * Retrieves the number of non-deleted items that belong to the specified product
     *
     * @param productId Product identifier
     * @return Number of items
     */
    long countActiveItemsByProduct(String productId);

    /**
     * Retrieves all currently active categories
     *
     * @return A list containing all active categories
     */
    CategoryTree getActiveCategoryTree();

    /**
     * Retrives all active products matching the specified criteria
     *
     * @param query Search criteria
     * @return A list containing all matching active products
     */
    List<Product> listActiveProducts(ProductQuery query);

    /**
     * Lists products ordered by score
     *
     * @param limit Maximum number of products to load
     * @return A List of products ordered by score descending
     */
    List<Product> listTopProducts(int limit);
}
