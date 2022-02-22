package edu.uoc.pdp.core.dao;

import edu.uoc.pdp.db.entity.Brand;
import edu.uoc.pdp.db.entity.Brand_;

import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.util.Collections;
import java.util.List;

@Singleton
public class BrandDAO extends BaseDAO<Brand> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Order> defaultOrder(Root<Brand> root, CriteriaBuilder builder) {
        return Collections.singletonList(builder.asc(root.get(Brand_.NAME)));
    }

    /**
     * Checks if a brand with different id and the same name already exists
     *
     * @param brand Brand to check
     * @return {@code true} if a brand like the specified one already exists, {@code false} otherwise
     */
    public boolean existsBrand(Brand brand) {
        return exists((root, builder) -> builder.and(
                builder.notEqual(root.get(Brand_.ID), brand.getId() == null ? "" : brand.getId()),
                builder.equal(root.get(Brand_.NAME), brand.getName())
        ));
    }
}
