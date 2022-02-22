package edu.uoc.pdp.core.ejb.catalog;

import edu.uoc.pdp.core.dao.BrandDAO;
import edu.uoc.pdp.core.dao.ItemDAO;
import edu.uoc.pdp.core.dao.ModelDAO;
import edu.uoc.pdp.core.ejb.catalog.handler.CategoryHandler;
import edu.uoc.pdp.core.ejb.catalog.handler.ProductHandler;
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
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

import static edu.uoc.pdp.db.entity.UserRoles.ADMIN;


@Stateless
@PermitAll
public class CatalogFacadeImpl implements CatalogFacade {

    @Inject
    private CategoryHandler categoryHandler;
    @Inject
    private ProductHandler productHandler;
    @Inject
    private ModelDAO modelDAO;
    @Inject
    private BrandDAO brandDAO;
    @Inject
    private ItemDAO itemDAO;


    /**
     * {@inheritDoc}
     */
    @Override
    public Category getCategory(String categoryId) {
        Category category = categoryHandler.getCategory(categoryId);
        Hibernate.initialize(category.getParent());
        return category;
    }

    @Override
    @RolesAllowed(ADMIN)
    public void saveCategory(Category category) throws CategoryException {
        categoryHandler.save(category);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @RolesAllowed(ADMIN)
    public void deleteCategory(String categoryId) throws CategoryException {
        categoryHandler.delete(categoryId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Category> listAllCategories() {
        return categoryHandler.getAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model getModel(String modelId) {
        return modelDAO.getById(modelId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RolesAllowed(ADMIN)
    public void saveModel(Model model) throws ModelException {
        model.setName(model.getName().trim());

        if (modelDAO.existsModel(model)) {
            throw new ModelException("Ja existeix un model amb aquestes característiques.");
        }
        modelDAO.save(model);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RolesAllowed(ADMIN)
    public void deleteModel(String modelId) {
        modelDAO.delete(modelId);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Model> listAllModels() {
        return modelDAO.getAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Brand getBrand(String brandId) {
        return brandDAO.getById(brandId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RolesAllowed(ADMIN)
    public void saveBrand(Brand brand) throws BrandException {
        brand.setName(StringUtils.upperCase(brand.getName()).trim());

        if (brandDAO.existsBrand(brand)) {
            throw new BrandException("Ja existeix una marca amb aquestes característiques.");
        }
        brandDAO.save(brand);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RolesAllowed(ADMIN)
    public void deleteBrand(String brandId) {
        brandDAO.delete(brandId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Brand> listAllBrands() {
        return brandDAO.getAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @RolesAllowed(ADMIN)
    public void saveProduct(Product product, UploadedFile image) throws ImageException, ProductException {
        productHandler.save(product, image);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RolesAllowed(ADMIN)
    public void deleteProduct(String productId) {
        productHandler.delete(productId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Product> listAllProducts() {
        return productHandler.getAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Product getProduct(String productId) {
        return productHandler.getProduct(productId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Item> listAllItems() {
        return itemDAO.getAll();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Item getItem(String itemId) {
        return itemDAO.getById(itemId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RolesAllowed(ADMIN)
    public void saveItem(Item item) throws ItemException {
        item.setSerialNumber(item.getSerialNumber().trim());
        
        if (itemDAO.existsItem(item)) {
            throw new ItemException("Ja existeix un item amb aquestes característiques.");
        }
        itemDAO.save(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RolesAllowed(ADMIN)
    public void deleteItem(String itemId) {
        itemDAO.delete(itemId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Item> listItemsByProduct(String productId) {
        return itemDAO.getAllItems(productId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long countAvailableItemsByProduct(String productId) {
        return itemDAO.countAvailableItems(productId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long countActiveItemsByProduct(String productId) {
        return itemDAO.countActiveItems(productId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CategoryTree getActiveCategoryTree() {
        return categoryHandler.getActiveCategoryTree();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Product> listActiveProducts(ProductQuery query) {
        return productHandler.findActiveProducts(query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Product> listTopProducts(int limit) {
        return productHandler.listTopProducts(limit);
    }
}
