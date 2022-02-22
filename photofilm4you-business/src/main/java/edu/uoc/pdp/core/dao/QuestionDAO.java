package edu.uoc.pdp.core.dao;

import edu.uoc.pdp.db.entity.Question;
import edu.uoc.pdp.db.entity.Question_;

import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.util.Collections;
import java.util.List;

@Singleton
public class QuestionDAO extends BaseDAO<Question> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Order> defaultOrder(Root<Question> root, CriteriaBuilder builder) {
        return Collections.singletonList(builder.desc(root.get(Question_.CREATED)));
    }
}
