package edu.uoc.pdp.core.ejb.catalog.handler;

import edu.uoc.pdp.core.dao.ProductDAO;
import edu.uoc.pdp.core.ejb.media.handler.ImageHandler;
import edu.uoc.pdp.core.ejb.rent.cache.ProductCategoryCache;
import edu.uoc.pdp.core.exception.ImageException;
import edu.uoc.pdp.core.exception.ProductException;
import edu.uoc.pdp.core.model.availability.ProductQuery;
import edu.uoc.pdp.core.model.file.UploadedFile;
import edu.uoc.pdp.db.entity.Image;
import edu.uoc.pdp.db.entity.Product;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class ProductHandler {

    @Inject
    private ProductDAO productDAO;
    @Inject
    private ProductCategoryCache categoryCache;
    @Inject
    private ImageHandler imageHandler;


    /**
     * Returns all active products matching the specified criteria
     *
     * @param query Product query
     * @return A List containing all active products matching the request
     */
    public List<Product> findActiveProducts(ProductQuery query) {
        Set<String> ids = categoryCache.getActiveCategoryProducts(query.getCategory());

        return findProducts(ids, query);
    }

    /**
     * Lists products ordered by score
     *
     * @param limit Maximum number of products to load
     * @return A List of products ordered by score descending
     */
    public List<Product> listTopProducts(int limit) {
        return productDAO.findTopProducts(limit);
    }

    /**
     * Returns all active products
     *
     * @return A List with all active products
     */
    public List<Product> getActiveProducts() {
        return productDAO.getActiveProducts();
    }

    /**
     * Returns a product by id
     *
     * @param productId Product identifier
     * @return The matching product
     */
    public Product getProduct(String productId) {
        return productDAO.getById(productId);
    }

    /**
     * Deltes a product by id
     *
     * @param productId Product to be deleted
     */
    public void delete(String productId) {
        productDAO.delete(productId);
    }

    /**
     * Retrieves all products
     *
     * @return A list with all productss
     */
    public List<Product> getAll() {
        return productDAO.getAll();
    }

    /**
     * Persists a product along with its image if specified. If no image is present
     *
     * @param product Product
     * @param image   Uploaded image file
     */
    public void save(Product product, UploadedFile image) throws ImageException, ProductException {
        product.setName(product.getName().trim());

        if (productDAO.existsProduct(product)) {
            throw new ProductException("Ja existeix un producte amb aquestes característiques.");
        }
        Image lastImage = null;

        if (image != null) {
            lastImage = product.getImage();
            product.setImage(imageHandler.processImage(image));
        }
        productDAO.save(product);
        imageHandler.deleteImage(lastImage);
    }

    private List<Product> findProducts(Set<String> ids, ProductQuery query) {
        return productDAO.findProducts(ids, query.getBrand(), query.getModel()).stream()
                .filter(product -> matchesName(product, query.getName()))
                .collect(Collectors.toList());
    }

    private boolean matchesName(Product product, String filter) {
        if (StringUtils.isNotBlank(filter)) {
            String[] words = StringUtils.split(cleanText(filter), ' ');
            String name = cleanText(product.getName());

            return Arrays.stream(words).allMatch(name::contains);
        }
        return true;
    }

    private String cleanText(String description) {
        return StringUtils.stripAccents(StringUtils.normalizeSpace(description)).toLowerCase().trim();
    }

    void setProductDAO(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    void setCategoryCache(ProductCategoryCache categoryCache) {
        this.categoryCache = categoryCache;
    }

    void setImageHandler(ImageHandler imageHandler) {
        this.imageHandler = imageHandler;
    }
}
