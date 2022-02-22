package edu.uoc.pdp.core.ejb.rent.handler;

import edu.uoc.pdp.core.annotation.Created;
import edu.uoc.pdp.core.annotation.Removed;
import edu.uoc.pdp.core.configuration.ConfigurationProperties;
import edu.uoc.pdp.core.dao.RentDAO;
import edu.uoc.pdp.core.dao.ReservationDAO;
import edu.uoc.pdp.core.ejb.payment.PaymentFacade;
import edu.uoc.pdp.core.ejb.session.SessionManager;
import edu.uoc.pdp.core.exception.RentCancellationException;
import edu.uoc.pdp.core.exception.RentPaymentException;
import edu.uoc.pdp.core.exception.RentPenalizationException;
import edu.uoc.pdp.core.model.event.ItemEvent;
import edu.uoc.pdp.db.entity.Cancellation;
import edu.uoc.pdp.db.entity.Item;
import edu.uoc.pdp.db.entity.PenalizationStatus;
import edu.uoc.pdp.db.entity.Product;
import edu.uoc.pdp.db.entity.Rent;
import edu.uoc.pdp.db.entity.RentStatus;
import edu.uoc.pdp.db.entity.Reservation;
import org.hibernate.Hibernate;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static edu.uoc.pdp.db.entity.PenalizationStatus.PAID;
import static edu.uoc.pdp.db.entity.PenalizationStatus.PENDING;

@Singleton
public class RentHandler {

    @Inject
    private RentDAO rentDAO;
    @Inject
    private ReservationDAO reservationDAO;
    @Inject
    @Removed
    private Event<ItemEvent> itemRemovedEvent;
    @Inject
    @Created
    private Event<ItemEvent> itemCreatedEvent;
    @Inject
    private PaymentFacade paymentFacade;
    @Inject
    private ConfigurationProperties properties;
    @Inject
    private SessionManager sessionManager;


    /**
     * Confirms a rent object by assigning a new identifier and storing it into the database
     *
     * @param rent Rent to be confirmed
     * @return The persisted rent instance
     */
    @Transactional
    public String confirm(Rent rent) {
        rent.setId(generateRentId(rent));

        createReservations(rent);
        notifyChanges(itemRemovedEvent, rent);
        rentDAO.save(rent);

        return paymentFacade.requestPayment(rent.getId(), rent.getTotalPrice());
    }

    /**
     * Retrieves a rent with pending payment and returns a payment url
     *
     * @param rentId Identifier of the rent to be paid for
     * @return A payment url to redirect to
     */
    @Transactional
    public String requestPayment(String rentId) throws RentPaymentException {
        Rent rent = rentDAO.getByIdAndCustomer(rentId, sessionManager.getUserId());

        if (rent.getStatus() != RentStatus.NOT_CONFIRMED) {
            throw new RentPaymentException(rentId);
        }
        return paymentFacade.requestPayment(rent.getId(), rent.getTotalPrice());
    }

    /**
     * Cancels a rent by identifier. Items blocked by this booking will be considered available again during the
     * booking dates
     *
     * @param rentId Rent identifier
     * @throws RentCancellationException If the rent is already cancelled or is starting day is in the past
     */
    @Transactional
    public void cancel(String rentId) throws RentCancellationException {
        Rent rent = rentDAO.getById(rentId);

        if (!rent.isCancellable()) {
            throw new RentCancellationException(rentId);
        }
        rent.setCancellation(buildCancellation(rent));
        rent.setStatus(RentStatus.CANCELLED);

        rentDAO.save(rent);

        deleteReservations(rent);
        notifyChanges(itemCreatedEvent, rent);
    }

    /**
     * Retrieves all rents with pending cancellation payments
     *
     * @return A list with all matching rents
     */
    public List<Rent> listPendingPenalizations() {
        return rentDAO.findRentsWithPendingPenalty();
    }

    /**
     * Marks the cancellation of the specified rent as paid
     *
     * @param rentId Rent identifier
     * @throws RentPenalizationException if the rent has no cancellation or it has already been paid for
     */
    @Transactional
    public void closePenalization(String rentId) throws RentPenalizationException {
        Rent rent = rentDAO.getById(rentId);

        if (!hasPendingCancellation(rent)) {
            throw new RentPenalizationException(rentId);
        }
        rent.getCancellation().setStatus(PAID);

        rentDAO.save(rent);
    }

    /**
     * Retrieves a rent by its identifier. This method will filter by customer id if the requester is a customer,
     * only an admin can freely query rents
     *
     * @param rentId Rent identifier
     * @return A matching rent if any
     * @throws javax.persistence.EntityNotFoundException if there's no rent matching by id
     */
    public Rent getRent(String rentId) {
        Rent rent;
        if (sessionManager.isCustomer()) {
            rent = rentDAO.getByIdAndCustomer(rentId, sessionManager.getUserId());
        } else {
            rent = rentDAO.getById(rentId);
        }
        Hibernate.initialize(rent.getItems());

        return rent;
    }

    /**
     * Confirms the payment of a rent
     *
     * @param rentId Rent identifier
     */
    @Transactional
    public void confirmPayment(String rentId) {
        Rent rent = rentDAO.getById(rentId);
        rent.setStatus(RentStatus.CONFIRMED);

        rentDAO.save(rent);
    }

    /**
     * Retrieves the cancel penalty amount for a Rent
     *
     * @param rent Rent instance
     */
    public BigDecimal calculateCancelPenalty(Rent rent) {
        if (rent.getStatus() != RentStatus.NOT_CONFIRMED) {
            LocalDateTime checkin = rent.getFrom().atStartOfDay();
            LocalDateTime now = LocalDateTime.now();

            if (checkin.minusHours(properties.getCancellationHoursPenalty()).isBefore(now)) {
                return calculatePenalty(rent, properties.getCancellationHardPenalty());
            } else if (checkin.minusHours(properties.getCancellationHoursFree()).isBefore(now)) {
                return calculatePenalty(rent, properties.getCancellationSoftPenalty());
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * Retrieves all rents
     *
     * @return A list with all rents
     */
    public List<Rent> listAll() {
        return rentDAO.getAll();
    }

    /**
     * Retrieves all rents that have not started yet
     *
     * @return List of rents that haven't started yet
     */
    public List<Rent> listFutureRents() {
        return rentDAO.findFutureRents();
    }

    private boolean hasPendingCancellation(Rent rent) {
        return rent.getCancellation() != null && rent.getCancellation().getStatus() == PENDING;
    }

    private Cancellation buildCancellation(Rent rent) {
        BigDecimal penalty = calculateCancelPenalty(rent);
        PenalizationStatus status = penalty.compareTo(BigDecimal.ZERO) == 0 ? PAID : PENDING;

        Cancellation cancellation = new Cancellation();
        cancellation.setCreationDate(LocalDateTime.now());
        cancellation.setPenalization(penalty);
        cancellation.setStatus(status);

        return cancellation;
    }

    private BigDecimal calculatePenalty(Rent rent, String penalty) {
        if (penalty.contains("%")) {
            BigDecimal percentage = getValue(penalty.replace("%", ""));

            return rent.getTotalPrice()
                    .multiply(percentage)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return getValue(penalty).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getValue(String property) {
        return new BigDecimal(property.replace(",", ".").trim());
    }

    private String generateRentId(Rent rent) {
        String month = rent.getCreated().getMonth().name().substring(0, 2);
        String year = String.valueOf(rent.getCreated().getYear()).substring(2);
        long sequence = rentDAO.getNextId();

        return year + month + sequence;
    }

    private void createReservations(Rent rent) {
        Stream.iterate(rent.getFrom(), d -> d.plusDays(1))
                .limit(ChronoUnit.DAYS.between(rent.getFrom(), rent.getTo()) + 1)
                .flatMap(date -> rent.getItems().stream().map(item -> new Reservation(item, date)))
                .forEach(reservationDAO::save);
    }

    private void deleteReservations(Rent rent) {
        Set<String> items = rent.getItems().stream().map(Item::getId).collect(Collectors.toSet());

        reservationDAO.deleteReservations(items, rent.getFrom(), rent.getTo());
    }

    private void notifyChanges(Event<ItemEvent> event, Rent rent) {
        Map<Product, List<Item>> itemsByProduct = rent.getItemsByProduct();

        itemsByProduct.keySet().stream()
                .map(product -> new ItemEvent(product.getId(), rent.getFrom(), rent.getTo(), itemsByProduct.get(product).size()))
                .forEach(event::fire);
    }
}
