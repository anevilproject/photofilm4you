package edu.uoc.pdp.core.dao;

import edu.uoc.pdp.db.entity.Item_;
import edu.uoc.pdp.db.entity.Reservation;
import edu.uoc.pdp.db.entity.Reservation_;

import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Singleton
public class ReservationDAO extends BaseDAO<Reservation> {

    /**
     * Retrieves all reservations of a set of items that exist between two dates (inclusive)
     *
     * @param items Identifiers of the items the reservations must belong to
     * @param from  Filtered date from
     * @param to    Filtered date to
     * @return A list with all criteria matching reservations
     */
    public List<Reservation> findReservations(Set<String> items, LocalDate from, LocalDate to) {
        if (items.size() == 0) {
            return Collections.emptyList();
        }
        return findList((root, builder) -> buildPredicate(builder, root, items, from, to));
    }

    /**
     * Deletes all reservations that belong to the specified items between two dates
     *
     * @param items Item identifiers
     * @param from  Date from
     * @param to    Date to
     */
    public void deleteReservations(Set<String> items, LocalDate from, LocalDate to) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaDelete<Reservation> criteria = builder.createCriteriaDelete(Reservation.class);
        Root<Reservation> reservation = criteria.from(Reservation.class);
        Predicate predicate = buildPredicate(builder, reservation, items, from, to);

        entityManager.createQuery(criteria.where(predicate)).executeUpdate();
    }

    private Predicate buildPredicate(
            CriteriaBuilder builder,
            Root<Reservation> reservation,
            Set<String> items,
            LocalDate from,
            LocalDate to) {
        return builder.and(
                builder.between(reservation.get(Reservation_.DATE), from, to),
                reservation.get(Reservation_.ITEM).get(Item_.ID).in(items)
        );
    }
}
