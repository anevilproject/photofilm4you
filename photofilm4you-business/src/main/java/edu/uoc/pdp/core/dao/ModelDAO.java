package edu.uoc.pdp.core.dao;

import edu.uoc.pdp.db.entity.Brand_;
import edu.uoc.pdp.db.entity.Model;
import edu.uoc.pdp.db.entity.Model_;

import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.util.Arrays;
import java.util.List;

@Singleton
public class ModelDAO extends BaseDAO<Model> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Order> defaultOrder(Root<Model> root, CriteriaBuilder builder) {
        return Arrays.asList(
                builder.asc(root.get(Model_.BRAND).get(Brand_.NAME)),
                builder.asc(root.get(Model_.NAME)));
    }

    /**
     * Checks if a model with different id and the same name and brand already exists
     *
     * @param model Model to check
     * @return {@code true} if a model like the specified one already exists, {@code false} otherwise
     */
    public boolean existsModel(Model model) {
        return exists((root, builder) -> builder.and(
                builder.notEqual(root.get(Model_.ID), model.getId() == null ? "" : model.getId()),
                builder.equal(root.get(Model_.NAME), model.getName()),
                builder.equal(root.get(Model_.BRAND).get(Brand_.ID), model.getBrandId())
        ));
    }
}
