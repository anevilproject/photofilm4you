package edu.uoc.pdp.core.ejb.rent.job;

import edu.uoc.pdp.core.dao.RentDAO;
import edu.uoc.pdp.core.ejb.rent.handler.RentHandler;
import edu.uoc.pdp.core.exception.RentCancellationException;
import edu.uoc.pdp.db.entity.Rent;
import edu.uoc.pdp.db.entity.RentStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ejb.Timer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RentReleaseJobTest {

    @Mock
    private RentDAO rentDAO;
    @Mock
    private RentHandler rentHandler;
    @Mock
    private Timer timer;

    @InjectMocks
    private RentReleaseJob job;


    @Test
    public void executeMustCancelAndDeleteRentsPendingForMoreThan30Minutes() throws RentCancellationException {
        Rent rent = getRent();
        when(rentDAO.findUnconfirmedRents(any())).thenReturn(Collections.singletonList(rent));

        job.execute(timer);

        verify(rentDAO, times(1)).findUnconfirmedRents(any());
        verify(rentHandler, times(1)).cancel(rent.getId());
        verify(rentDAO, times(1)).delete(rent);
    }

    @Test
    public void executeMustIgnoreCancelErrors() throws RentCancellationException {
        Rent rent1 = getRent();
        rent1.setId("20DE88");
        Rent rent2 = getRent();

        when(rentDAO.findUnconfirmedRents(any())).thenReturn(Arrays.asList(rent1, rent2));
        doThrow(new RentCancellationException("")).when(rentHandler).cancel(rent1.getId());

        job.execute(timer);

        verify(rentHandler, times(1)).cancel(rent1.getId());
        verify(rentDAO, never()).delete(rent1);
        verify(rentHandler, times(1)).cancel(rent2.getId());
        verify(rentDAO, times(1)).delete(rent2);
    }

    @Test
    public void executeMustDoNothingIfNoPendingRents() {
        when(rentDAO.findUnconfirmedRents(any())).thenReturn(Collections.emptyList());

        job.execute(timer);

        verifyNoInteractions(rentHandler);
        verify(rentDAO, never()).delete((Rent) any());
    }

    private Rent getRent() {
        Rent rent = new Rent();
        rent.setId("20DE7");
        rent.setFrom(LocalDate.of(2020, 12, 2));
        rent.setTo(LocalDate.of(2020, 12, 4));
        rent.setTotalPrice(BigDecimal.valueOf(175.40));
        rent.setStatus(RentStatus.NOT_CONFIRMED);
        rent.setCreated(LocalDate.of(2020, 12, 2).atStartOfDay());

        return rent;
    }
}
