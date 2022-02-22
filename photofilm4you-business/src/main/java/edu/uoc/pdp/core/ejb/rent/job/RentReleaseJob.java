package edu.uoc.pdp.core.ejb.rent.job;

import edu.uoc.pdp.core.dao.RentDAO;
import edu.uoc.pdp.core.ejb.rent.handler.RentHandler;
import edu.uoc.pdp.db.entity.Rent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timer;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;

@Startup
@Singleton
public class RentReleaseJob {

    private static final Logger log = LoggerFactory.getLogger(RentReleaseJob.class);

    @Inject
    private RentDAO rentDAO;
    @Inject
    private RentHandler rentHandler;


    /**
     * Every 30 minutes releases all rents that are created and not paid for in order to release the stock being locked
     *
     * @param timer Execution timer
     */
    @Schedule(hour = "*", minute = "*/5", info = "Remove unpaid rents every 30 minutes to release locked stock", persistent = false)
    public void execute(Timer timer) {
        log.info("Started executing rent release job");

        List<Rent> rents = rentDAO.findUnconfirmedRents(LocalDateTime.now().minusMinutes(30));

        if (rents.isEmpty()) {
            log.info("There are no rents pending payment");
        } else {
            long init = System.currentTimeMillis();
            int released = rents.stream().mapToInt(this::release).sum();

            log.info("Released {} rents in {} ms", released, System.currentTimeMillis() - init);
        }
        log.info("Next release execution scheduled for {}", timer.getNextTimeout());
    }

    private int release(Rent rent) {
        try {
            rentHandler.cancel(rent.getId());
            rentDAO.delete(rent);

            return 1;
        } catch (Exception e) {
            log.error("Rent {} could not be released!", rent.getId(), e);
        }
        return 0;
    }
}
