package edu.uoc.pdp.core.ejb.rent.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
import edu.uoc.pdp.db.entity.ItemStatus;
import edu.uoc.pdp.db.entity.PenalizationStatus;
import edu.uoc.pdp.db.entity.Product;
import edu.uoc.pdp.db.entity.Rent;
import edu.uoc.pdp.db.entity.RentStatus;
import edu.uoc.pdp.db.entity.Reservation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.enterprise.event.Event;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static edu.uoc.pdp.db.entity.PenalizationStatus.PAID;
import static edu.uoc.pdp.db.entity.RentStatus.CONFIRMED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RentHandlerTest {

    @Mock
    private RentDAO rentDAO;
    @Mock
    private ReservationDAO reservationDAO;
    @Mock
    @Removed
    private Event<ItemEvent> itemRemovedEvent;
    @Mock
    @Created
    private Event<ItemEvent> itemCreatedEvent;
    @Mock
    private PaymentFacade paymentFacade;
    @Mock
    private SessionManager sessionManager;
    @Mock(lenient = true)
    private ConfigurationProperties properties;

    @InjectMocks
    private RentHandler rentHandler;


    @Test
    public void confirmMustCreateAReservationForEachItemAndDay() {
        Rent rent = getRent();
        when(rentDAO.getNextId()).thenReturn(7L);

        rentHandler.confirm(rent);

        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationDAO, times(9)).save(reservationCaptor.capture());

        Set<Reservation> storedReservations = new HashSet<>(reservationCaptor.getAllValues());
        Set<Reservation> expectedReservations = Sets.newHashSet(
                new Reservation(getItem(1, 1), LocalDate.of(2020, 12, 2)),
                new Reservation(getItem(2, 1), LocalDate.of(2020, 12, 2)),
                new Reservation(getItem(3, 2), LocalDate.of(2020, 12, 2)),
                new Reservation(getItem(1, 1), LocalDate.of(2020, 12, 3)),
                new Reservation(getItem(2, 1), LocalDate.of(2020, 12, 3)),
                new Reservation(getItem(3, 2), LocalDate.of(2020, 12, 3)),
                new Reservation(getItem(1, 1), LocalDate.of(2020, 12, 4)),
                new Reservation(getItem(2, 1), LocalDate.of(2020, 12, 4)),
                new Reservation(getItem(3, 2), LocalDate.of(2020, 12, 4))
        );
        assertEquals(expectedReservations, storedReservations);
    }

    @Test
    public void confirmMustPersistTheRentInstance() {
        Rent rent = getRent();
        when(rentDAO.getNextId()).thenReturn(7L);

        rentHandler.confirm(rent);

        verify(rentDAO, times(1)).save(rent);
    }

    @Test
    public void confirmMustFireItemRemovedEvents() {
        Rent rent = getRent();
        when(rentDAO.getNextId()).thenReturn(7L);

        rentHandler.confirm(rent);

        ArgumentCaptor<ItemEvent> eventCaptor = ArgumentCaptor.forClass(ItemEvent.class);
        verify(itemRemovedEvent, times(2)).fire(eventCaptor.capture());

        Set<ItemEvent> firedEvents = new HashSet<>(eventCaptor.getAllValues());
        Set<ItemEvent> expectedEvents = Sets.newHashSet(
                new ItemEvent(
                        "PRODUCT1",
                        LocalDate.of(2020, 12, 2),
                        LocalDate.of(2020, 12, 4),
                        2),
                new ItemEvent(
                        "PRODUCT2",
                        LocalDate.of(2020, 12, 2),
                        LocalDate.of(2020, 12, 4),
                        1)
        );
        assertEquals(expectedEvents, firedEvents);
    }

    @Test
    public void confirmMustInvokePaymentFacade() {
        Rent rent = getRent();
        when(rentDAO.getNextId()).thenReturn(7L);
        when(paymentFacade.requestPayment("20DE7", BigDecimal.valueOf(175.40))).thenReturn("http://test-url.com/test");

        String redirect = rentHandler.confirm(rent);

        assertEquals("http://test-url.com/test", redirect);
    }

    @Test
    public void requestPaymentMustInvokePaymentFacade() throws RentPaymentException {
        when(sessionManager.getUserId()).thenReturn("customer");
        when(paymentFacade.requestPayment("20DE7", BigDecimal.valueOf(175.40))).thenReturn("http://test-url.com/test");
        when(rentDAO.getByIdAndCustomer("20DE7", "customer")).thenReturn(getRent());

        String redirect = rentHandler.requestPayment("20DE7");

        assertEquals("http://test-url.com/test", redirect);
    }

    @Test(expected = RentPaymentException.class)
    public void requestPaymentMustThrowExceptionIfRentIsCancelled() throws RentPaymentException {
        when(sessionManager.getUserId()).thenReturn("customer");
        when(rentDAO.getByIdAndCustomer("20DE7", "customer")).thenReturn(getCancelledRent());

        rentHandler.requestPayment("20DE7");
    }

    @Test(expected = RentCancellationException.class)
    public void cancelMustThrowExceptionIfRentIsAlreadyCancelled() throws RentCancellationException {
        Rent rent = getRent();
        rent.setCancellation(new Cancellation());

        when(rentDAO.getById("RENTID")).thenReturn(rent);

        rentHandler.cancel("RENTID");
    }

    @Test(expected = RentCancellationException.class)
    public void cancelMustThrowExceptionIfRentHasAlreadyStarted() throws RentCancellationException {
        Rent rent = getRent();
        rent.setFrom(LocalDate.now().minusDays(1));

        when(rentDAO.getById("RENTID")).thenReturn(rent);

        rentHandler.cancel("RENTID");
    }

    @Test
    public void cancelMustFireItemCreationEvents() throws RentCancellationException {
        mockCancelProperties();
        when(rentDAO.getById("RENTID")).thenReturn(getCancellationRent(LocalDate.now().plusDays(3)));

        rentHandler.cancel("RENTID");

        ArgumentCaptor<ItemEvent> eventCaptor = ArgumentCaptor.forClass(ItemEvent.class);
        verify(itemCreatedEvent, times(2)).fire(eventCaptor.capture());

        Set<ItemEvent> firedEvents = new HashSet<>(eventCaptor.getAllValues());
        Set<ItemEvent> expectedEvents = Sets.newHashSet(
                new ItemEvent(
                        "PRODUCT1",
                        LocalDate.now().plusDays(3),
                        LocalDate.now().plusDays(5),
                        2),
                new ItemEvent(
                        "PRODUCT2",
                        LocalDate.now().plusDays(3),
                        LocalDate.now().plusDays(5),
                        1)
        );
        assertEquals(expectedEvents, firedEvents);
    }

    @Test
    public void cancelMustDeleteRelatedReservations() throws RentCancellationException {
        mockCancelProperties();
        when(rentDAO.getById("RENTID")).thenReturn(getCancellationRent(LocalDate.now().plusDays(3)));

        rentHandler.cancel("RENTID");

        verify(reservationDAO, times(1)).deleteReservations(
                Sets.newHashSet("ITEM1", "ITEM2", "ITEM3"), LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));
    }

    @Test
    public void cancelMustPersistCancelledBooking() throws RentCancellationException {
        Rent rent = getCancellationRent(LocalDate.now().plusDays(3));
        mockCancelProperties();
        when(rentDAO.getById("RENTID")).thenReturn(rent);

        rentHandler.cancel("RENTID");

        verify(rentDAO, times(1)).save(rent);
        assertNotNull(rent.getCancellation());
        assertEquals(LocalDate.now(), rent.getCancellation().getCreationDate().toLocalDate());
        assertEquals(RentStatus.CANCELLED, rent.getStatus());
    }

    @Test
    public void cancelMustNotPenalizeCancellationsWithMoreThanFreeConfiguredHours() throws RentCancellationException {
        Rent rent = getCancellationRent(LocalDate.now().plusDays(3));
        mockCancelProperties();
        when(rentDAO.getById("RENTID")).thenReturn(rent);

        rentHandler.cancel("RENTID");

        Cancellation cancellation = rent.getCancellation();

        assertEquals(PAID, cancellation.getStatus());
        assertEquals(BigDecimal.ZERO, cancellation.getPenalization());
    }

    @Test
    public void cancelMustApplySoftPenaltyOnCancellationsWithLessThanFreeHours() throws RentCancellationException {
        Rent rent = getCancellationRent(LocalDate.now().plusDays(2));
        mockCancelProperties();
        when(rentDAO.getById("RENTID")).thenReturn(rent);

        rentHandler.cancel("RENTID");

        Cancellation cancellation = rent.getCancellation();

        assertEquals(PenalizationStatus.PENDING, cancellation.getStatus());
        assertEquals(BigDecimal.valueOf(200, 2), cancellation.getPenalization());
    }

    @Test
    public void cancelMustApplyHardPenaltyOnCancellationsWithLessThanPenaltyHours() throws RentCancellationException {
        Rent rent = getCancellationRent(LocalDate.now());
        mockCancelProperties();
        when(rentDAO.getById("RENTID")).thenReturn(rent);

        rentHandler.cancel("RENTID");

        Cancellation cancellation = rent.getCancellation();

        assertEquals(PenalizationStatus.PENDING, cancellation.getStatus());
        assertEquals(BigDecimal.valueOf(4385, 2), cancellation.getPenalization());
    }

    @Test
    public void cancelMustNotApplyPenaltyIfRentIsNotConfirmed() throws RentCancellationException {
        Rent rent = getCancellationRent(LocalDate.now());
        rent.setStatus(RentStatus.NOT_CONFIRMED);
        mockCancelProperties();
        when(rentDAO.getById("RENTID")).thenReturn(rent);

        rentHandler.cancel("RENTID");

        Cancellation cancellation = rent.getCancellation();

        assertEquals(PAID, cancellation.getStatus());
        assertEquals(BigDecimal.ZERO, cancellation.getPenalization());
    }

    @Test(expected = RentPenalizationException.class)
    public void closePenalizationMustThrowExceptionIfRentIsNotCancelled() throws RentPenalizationException {
        when(rentDAO.getById("RENTID")).thenReturn(getRent());

        rentHandler.closePenalization("RENTID");
    }

    @Test(expected = RentPenalizationException.class)
    public void closePenalizationMustThrowExceptionIfRentCancellationIsAlreadyPaid() throws RentPenalizationException {
        Rent rent = getCancelledRent();
        rent.getCancellation().setStatus(PAID);

        when(rentDAO.getById("RENTID")).thenReturn(rent);

        rentHandler.closePenalization("RENTID");
    }

    @Test
    public void closePenalizationMustSetStatusToPaidAndPersist() throws RentPenalizationException {
        Rent rent = getCancelledRent();

        when(rentDAO.getById("RENTID")).thenReturn(rent);

        rentHandler.closePenalization("RENTID");

        assertEquals(PAID, rent.getCancellation().getStatus());
        verify(rentDAO, times(1)).save(rent);
    }

    @Test
    public void listPendingPenalizationsMustInvokeDaoSearch() {
        List<Rent> pending = Lists.newArrayList(getCancelledRent());
        when(rentDAO.findRentsWithPendingPenalty()).thenReturn(pending);

        List<Rent> result = rentHandler.listPendingPenalizations();

        verify(rentDAO, times(1)).findRentsWithPendingPenalty();
        assertEquals(pending, result);
    }

    @Test
    public void getRentQueriesByCustomerIdIfRequesterIsCustomer() {
        Rent rent = getRent();

        when(sessionManager.isCustomer()).thenReturn(true);
        when(sessionManager.getUserId()).thenReturn("customer54");
        when(rentDAO.getByIdAndCustomer("20DE8", "customer54")).thenReturn(rent);

        Rent result = rentHandler.getRent("20DE8");

        assertSame(rent, result);
    }

    @Test
    public void getRentQueriesByIdIfRequesterIsAdmin() {
        Rent rent = getRent();

        when(sessionManager.isCustomer()).thenReturn(false);
        when(rentDAO.getById("20DE8")).thenReturn(rent);

        Rent result = rentHandler.getRent("20DE8");

        assertSame(rent, result);
    }

    @Test
    public void confirmPaymentSetsStatusToConfirmed() {
        Rent rent = getRent();

        when(rentDAO.getById("20DE8")).thenReturn(rent);

        rentHandler.confirmPayment("20DE8");

        assertEquals(CONFIRMED, rent.getStatus());
    }

    @Test
    public void listAllInvokesDAO() {
        List<Rent> rents = Lists.newArrayList(getRent());
        when(rentDAO.getAll()).thenReturn(rents);

        List<Rent> result = rentHandler.listAll();

        assertSame(rents, result);
    }

    @Test
    public void listFutureRentsInvokesDAO() {
        List<Rent> rents = Lists.newArrayList(getRent());
        when(rentDAO.findFutureRents()).thenReturn(rents);

        List<Rent> result = rentHandler.listFutureRents();

        assertSame(rents, result);
    }

    private void mockCancelProperties() {
        when(properties.getCancellationHoursFree()).thenReturn(48);
        when(properties.getCancellationHoursPenalty()).thenReturn(24);
        when(properties.getCancellationHardPenalty()).thenReturn("25%");
        when(properties.getCancellationSoftPenalty()).thenReturn("2");
    }

    private Rent getCancelledRent() {
        Cancellation cancellation = new Cancellation();
        cancellation.setStatus(PenalizationStatus.PENDING);

        Rent rent = getRent();
        rent.setStatus(RentStatus.CANCELLED);
        rent.setCancellation(cancellation);

        return rent;
    }

    private Rent getCancellationRent(LocalDate from) {
        Rent rent = getRent();
        rent.setFrom(from);
        rent.setTo(from.plusDays(2));
        rent.setStatus(CONFIRMED);

        return rent;
    }

    private Rent getRent() {
        Rent rent = new Rent();
        rent.setId("20DE7");
        rent.setFrom(LocalDate.of(2020, 12, 2));
        rent.setTo(LocalDate.of(2020, 12, 4));
        rent.setTotalPrice(BigDecimal.valueOf(175.40));
        rent.setStatus(RentStatus.NOT_CONFIRMED);
        rent.setCreated(LocalDate.of(2020, 12, 2).atStartOfDay());
        rent.setItems(Lists.newArrayList(
                getItem(1, 1),
                getItem(2, 1),
                getItem(3, 2)));

        return rent;
    }

    private Item getItem(int itemNum, int productNum) {
        Item item = new Item();
        item.setId("ITEM" + itemNum);
        item.setProduct(getProduct(productNum));
        item.setSerialNumber(UUID.randomUUID().toString());
        item.setStatus(ItemStatus.OPERATIONAL);

        return item;
    }

    private Product getProduct(int index) {
        Product product = new Product();
        product.setId("PRODUCT" + index);

        return product;
    }
}
