package edu.uoc.pdp.core.dao;


import edu.uoc.pdp.db.entity.ProductRating;
import edu.uoc.pdp.db.entity.ProductRating_;
import edu.uoc.pdp.db.entity.Product_;

import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.util.Collections;
import java.util.List;


@Singleton
public class ProductRatingDAO extends BaseDAO<ProductRating> {

    /**
     * find all comments by a given product, ordered by created desc
     *
     * @param productId product from is asked to finde their comments
     * @return a list of productrating
     */
    public List<ProductRating> findCommentsByProduct(String productId) {
        return findList((root, builder) -> builder.equal(root.get(ProductRating_.PRODUCT).get(Product_.ID), productId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Order> defaultOrder(Root<ProductRating> root, CriteriaBuilder builder) {
        return Collections.singletonList(builder.desc(root.get(ProductRating_.CREATED)));
    }
}
