package edu.uoc.pdp.core.dao;

import com.google.common.collect.Lists;
import edu.uoc.pdp.db.entity.Cancellation_;
import edu.uoc.pdp.db.entity.Customer_;
import edu.uoc.pdp.db.entity.Rent;
import edu.uoc.pdp.db.entity.RentStatus;
import edu.uoc.pdp.db.entity.Rent_;

import javax.inject.Singleton;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiFunction;

import static edu.uoc.pdp.db.entity.PenalizationStatus.PENDING;

@Singleton
public class RentDAO extends BaseDAO<Rent> {

    /**
     * Retrieves the next value of the sequence used to generate rent locators.
     *
     * @return Next sequence value
     */
    public long getNextId() {
        Query query = entityManager.createNativeQuery("select nextval('{h-schema}seq_rent_id')");

        return ((BigInteger) query.getSingleResult()).longValue();
    }

    /**
     * Returns a rent matching by customer email and locator
     *
     * @param rentId Rent locator
     * @param email  Customer email
     * @return A matching Rent instance
     * @throws javax.persistence.NoResultException if there is no matching rent
     */
    public Rent getByIdAndCustomer(String rentId, String email) {
        return findOne((rent, builder) -> builder.and(
                builder.equal(rent.get(Rent_.ID), rentId),
                builder.equal(rent.get(Rent_.CUSTOMER).get(Customer_.EMAIL), email)));
    }

    /**
     * Retrieves all rents with a pending cancellation penalty
     *
     * @return All matching rents
     */
    public List<Rent> findRentsWithPendingPenalty() {
        return findRents((rent, builder) -> builder.equal(rent.get(Rent_.CANCELLATION).get(Cancellation_.STATUS), PENDING));
    }

    /**
     * Retrieves all rents made by a customer
     *
     * @param email Customer email
     * @return All matching rents
     */
    public List<Rent> findRentsByUser(String email) {
        return findList((rent, builder) -> builder.equal(rent.get(Rent_.CUSTOMER).get(Customer_.EMAIL), email));
    }

    /**
     * Retrieves all active rents. Active rents are those that have a future date and are not cancelled
     *
     * @return All matching rents
     */
    public List<Rent> findActiveRents() {
        return findRents((rent, builder) -> builder.and(
                builder.greaterThanOrEqualTo(rent.get(Rent_.TO), LocalDate.now()),
                builder.lessThanOrEqualTo(rent.get(Rent_.FROM), LocalDate.now()),
                builder.isNull(rent.get(Rent_.CANCELLATION))));
    }

    /**
     * Retrieves all confirmed rents that have not started yet
     *
     * @return List of rents that haven't started yet
     */
    public List<Rent> findFutureRents() {
        return findRents((rent, builder) -> builder.and(
                builder.greaterThan(rent.get(Rent_.FROM), LocalDate.now()),
                builder.isNull(rent.get(Rent_.CANCELLATION))));
    }

    /**
     * Returns all rents that are pending payment and that were created before the specified date
     *
     * @param maxCreationDate Creation date filter
     * @return A list of matching rents
     */
    public List<Rent> findUnconfirmedRents(LocalDateTime maxCreationDate) {
        return findList((rent, builder) -> builder.and(
                builder.lessThan(rent.get(Rent_.CREATED), maxCreationDate),
                builder.equal(rent.get(Rent_.status), RentStatus.NOT_CONFIRMED)));
    }

    @Override
    protected List<Order> defaultOrder(Root<Rent> root, CriteriaBuilder builder) {
        return Lists.newArrayList(builder.desc(root.get(Rent_.CREATED)));
    }

    private List<Rent> findRents(BiFunction<Root<Rent>, CriteriaBuilder, Predicate> predicateSupplier) {
        return findList((root, builder) -> builder.and(
                predicateSupplier.apply(root, builder),
                builder.notEqual(root.get(Rent_.status), RentStatus.NOT_CONFIRMED)));
    }
}
