package edu.uoc.pdp.core.dao;

import edu.uoc.pdp.core.model.event.CategoryIndexEvent;
import edu.uoc.pdp.db.entity.Category;
import edu.uoc.pdp.db.entity.Category_;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Singleton
public class CategoryDAO extends BaseDAO<Category> {

    @Inject
    private Event<CategoryIndexEvent> categoryIndexEvent;


    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Category save(Category category) {
        notifyChanges(category);

        return super.save(category);
    }

    /**
     * Checks if a category has children
     *
     * @param categoryId Category
     * @return {@code true} if the category has children, {@code false} otherwise
     */
    public boolean isParent(String categoryId) {
        return !findList(
                (root, builder) -> builder.equal(root.get(Category_.PARENT).get(Category_.ID), categoryId))
                .isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Order> defaultOrder(Root<Category> root, CriteriaBuilder builder) {
        return Collections.singletonList(builder.asc(root.get(Category_.NAME)));
    }

    private void notifyChanges(Category category) {
        if (category.getId() != null) {
            Category old = getById(category.getId());

            if (!Objects.equals(old.getParentId(), category.getParentId())) {
                categoryIndexEvent.fire(new CategoryIndexEvent());
            }
        }
    }
}
