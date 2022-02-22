package edu.uoc.pdp.core.dao;

import edu.uoc.pdp.db.entity.Customer_;
import edu.uoc.pdp.db.entity.Question_;
import edu.uoc.pdp.db.entity.Response;
import edu.uoc.pdp.db.entity.ResponseStatus;
import edu.uoc.pdp.db.entity.Response_;
import edu.uoc.pdp.db.entity.User_;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Collections;
import java.util.List;

import static edu.uoc.pdp.db.entity.ResponseStatus.APPROVED;

@Singleton
public class ResponseDAO extends BaseDAO<Response> {

    /**
     * Retrieves all responses for a question matching a status if specified
     *
     * @param questionId    Question identifier
     * @param customerEmail Requesting customer email, null for admins
     * @param onlyApproved  Whether all responses should be included or only approved ones
     * @return A list with all matching responses
     */
    public List<Response> findAllQuestionResponses(String questionId, String customerEmail, boolean onlyApproved) {
        return findList((root, builder) -> {
            Predicate predicate = builder.equal(root.get(Response_.QUESTION).get(Question_.ID), questionId);
            if (StringUtils.isNotBlank(customerEmail)) {
                predicate = builder.and(predicate, builder.or(
                        builder.equal(root.get(Response_.USER).get(User_.EMAIL), customerEmail),
                        builder.equal(root.get(Response_.STATUS), APPROVED)
                ));
            } else if (onlyApproved) {
                predicate = builder.and(predicate, builder.equal(root.get(Response_.STATUS), APPROVED));
            }
            return predicate;
        });
    }

    /**
     * Retrieves all responses posted by a customer
     *
     * @param email Customer email
     * @return A list containing the matching responses
     */
    public List<Response> findAllCustomerResponses(String email) {
        return findList((root, builder) -> builder.equal(root.get(Response_.USER).get(Customer_.EMAIL), email));
    }

    /**
     * Counts the number of responses for a question
     *
     * @param questionId   Question id
     * @param onlyApproved Whether all responses should be included or only approved ones
     * @return Number of responses for a question
     */
    public long countResponses(String questionId, boolean onlyApproved) {
        return count((root, builder) -> {
            Predicate predicate = builder.equal(root.get(Response_.QUESTION).get(Question_.ID), questionId);

            if (onlyApproved) {
                predicate = builder.and(predicate, builder.equal(root.get(Response_.STATUS), APPROVED));
            }
            return predicate;
        });
    }

    /**
     * Retrieves all responses to accept
     *
     * @return A list containing the matching responses
     */
    public List<Response> findAllResponsesToAccept() {
        return findList((root, builder) -> builder.equal(root.get(Response_.STATUS), ResponseStatus.PENDING));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Order> defaultOrder(Root<Response> root, CriteriaBuilder builder) {
        return Collections.singletonList(builder.desc(root.get(Response_.CREATED)));
    }
}
