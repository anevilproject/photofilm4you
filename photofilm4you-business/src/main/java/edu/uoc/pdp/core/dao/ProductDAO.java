package edu.uoc.pdp.core.dao;

import com.google.common.collect.Lists;
import edu.uoc.pdp.core.model.event.CategoryIndexEvent;
import edu.uoc.pdp.db.entity.Brand_;
import edu.uoc.pdp.db.entity.Category_;
import edu.uoc.pdp.db.entity.Model;
import edu.uoc.pdp.db.entity.Model_;
import edu.uoc.pdp.db.entity.Product;
import edu.uoc.pdp.db.entity.Product_;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Singleton;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Singleton
public class ProductDAO extends BaseDAO<Product> {


    @Override
    @Transactional
    public Product save(Product product) {
        notifyChanges(product);

        return super.save(product);
    }

    /**
     * Fetches all non-deleted products
     *
     * @return A list containing all active products
     */
    public List<Product> getActiveProducts() {
        return findList((root, builder) -> builder.isNull(root.get(Product_.DELETED)));
    }

    /**
     * Retrieves all products matching the specified criteria. Brand and model filters are only applied if not null.
     *
     * @param ids     Identifiers of the products to fetch
     * @param brandId Brand filter
     * @param modelId Model filter
     * @return A list containing matching products
     */
    public List<Product> findProducts(Set<String> ids, String brandId, String modelId) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> criteria = builder.createQuery(Product.class);
        Root<Product> product = criteria.from(Product.class);
        Join<Product, Model> model = product.join(Product_.MODEL, JoinType.INNER);
        Predicate predicate = product.get(Product_.ID).in(ids);

        if (StringUtils.isNotBlank(brandId)) {
            predicate = builder.and(predicate, builder.equal(model.get(Model_.BRAND).get(Brand_.ID), brandId));
        }
        if (StringUtils.isNotBlank(modelId)) {
            predicate = builder.and(predicate, builder.equal(model.get(Model_.ID), modelId));
        }
        return entityManager.createQuery(criteria.select(product).where(predicate)).getResultList();
    }

    /**
     * update the product's rating based on the average of its dependant ProductRatings
     *
     * @param productId the product to set the new average
     */
    public void updateRating(String productId) {
        Query query = entityManager.createQuery("UPDATE Product p SET rating = " +
                "(SELECT avg(rating) from ProductRating pr WHERE pr.product.id = :productId) " +
                "WHERE p.id = :productId");
        query.setParameter("productId", productId);

        query.executeUpdate();
    }


    /**
     * Lists products ordered by score
     *
     * @param limit Maximum number of products to load
     * @return A List of products ordered by score descending
     */
    public List<Product> findTopProducts(int limit) {
        return buildQuery(
                (root, builder) -> builder.and(
                        builder.isNull(root.get(Product_.DELETED)),
                        builder.isNotNull(root.get(Product_.RATING)
                        )),
                (root, builder) -> Lists.newArrayList(builder.desc(root.get(Product_.RATING))))
                .setMaxResults(limit)
                .getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Order> defaultOrder(Root<Product> root, CriteriaBuilder builder) {
        return Arrays.asList(
                builder.asc(root.get(Product_.CATEGORY).get(Category_.NAME)),
                builder.asc(root.get(Product_.MODEL).get(Model_.BRAND).get(Brand_.NAME)),
                builder.asc(root.get(Product_.MODEL).get(Model_.NAME)),
                builder.asc(root.get(Product_.NAME)));
    }

    /**
     * Checks if a product with different id and the same name and model already exists
     *
     * @param product Product to check
     * @return {@code true} if a product like the specified one already exists, {@code false} otherwise
     */
    public boolean existsProduct(Product product) {
        return exists((root, builder) -> builder.and(
                builder.notEqual(root.get(Product_.ID), product.getId() == null ? "" : product.getId()),
                builder.equal(root.get(Product_.NAME), product.getName()),
                builder.equal(root.get(Product_.MODEL).get(Model_.ID), product.getModelId())
        ));
    }

    private void notifyChanges(Product product) {
        if (product.getId() == null) {
            categoryIndexEvent.fire(new CategoryIndexEvent());
        } else {
            Product old = getById(product.getId());

            if (!Objects.equals(old.getCategory().getId(), product.getCategory().getId())
                    || !Objects.equals(old.getDeleted(), product.getDeleted())) {
                categoryIndexEvent.fire(new CategoryIndexEvent());
            }
        }
    }
}
