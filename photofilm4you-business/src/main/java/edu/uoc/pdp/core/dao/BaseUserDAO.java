package edu.uoc.pdp.core.dao;

import edu.uoc.pdp.db.entity.User;
import edu.uoc.pdp.db.entity.User_;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.util.Collections;
import java.util.List;

public class BaseUserDAO<T extends User> extends BaseDAO<T> {

    /**
     * Retrieves a user by email
     *
     * @param email User email
     * @return The matching user if any, {@code null} otherwise
     */
    public T getByEmail(String email) {
        return findOne((user, builder) -> builder.equal(user.get(User_.EMAIL), email));
    }

    @Override
    protected List<Order> defaultOrder(Root<T> root, CriteriaBuilder builder) {
        return Collections.singletonList(builder.desc(root.get(User_.DATE)));
    }
}
