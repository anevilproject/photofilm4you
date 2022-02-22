package edu.uoc.pdp.core.ejb.rent.cache;

import edu.uoc.pdp.core.annotation.Created;
import edu.uoc.pdp.core.annotation.Removed;
import edu.uoc.pdp.core.dao.ItemDAO;
import edu.uoc.pdp.core.dao.ProductDAO;
import edu.uoc.pdp.core.dao.ReservationDAO;
import edu.uoc.pdp.core.model.availability.IndexedProduct;
import edu.uoc.pdp.core.model.event.ItemEvent;
import edu.uoc.pdp.db.entity.Item;
import edu.uoc.pdp.db.entity.Product;
import edu.uoc.pdp.db.entity.Reservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.PermitAll;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.enterprise.event.TransactionPhase.AFTER_SUCCESS;

/**
 * This component caches all available inventory for a year in order to speed up availability queries.
 * <p>
 * Available units are indexed by category and product as well as dates.
 */
@Startup
@PermitAll
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class ProductAvailabilityCache {

    private static final Logger log = LoggerFactory.getLogger(ProductAvailabilityCache.class);

    /**
     * Number of days indexed by this cache from today (ideally it would be a config prop)
     */
    private static final int INDEXED_DAYS = 365;
    /**
     * Number of days to process reservation batches to calculate available inventory
     */
    private static final int RESERVATION_BATCH_SIZE = 20;

    private final Map<LocalDate, AvailableProductIndex> availabilityCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Inject
    private ProductDAO productDAO;
    @Inject
    private ItemDAO itemDAO;
    @Inject
    private ReservationDAO reservationDAO;


    /**
     * Calculates and stores inventory for a whole year since startup.
     * <p>
     * Note: to optimize this cache should be distributed and remote (or even persisted), but for this case a local cache will suffice
     */
    @PostConstruct
    public void load() {
        log.info("Started availability cache load");

        long init = System.currentTimeMillis();

        LocalDate today = LocalDate.now();

        Stream.iterate(today, d -> d.plusDays(1))
                .limit(INDEXED_DAYS)
                .forEach(date -> availabilityCache.put(date, new AvailableProductIndex()));

        productDAO.getActiveProducts().forEach(product -> load(product, today, today.plusDays(INDEXED_DAYS - 1)));

        scheduler.scheduleAtFixedRate(this::shift, 24, 24, TimeUnit.HOURS);

        log.info("Finished availability cache load in {} ms", System.currentTimeMillis() - init);
    }

    @PreDestroy
    public void destroy() {
        scheduler.shutdownNow();
    }

    /**
     * Listens to incoming events triggered by an item becoming unavailable. These events are fired by the persistence layer.
     *
     * @param event Fired event
     */
    public void itemRemoved(@Observes(during = AFTER_SUCCESS) @Removed ItemEvent event) {
        getIndexes(event).forEach(index -> index.removeUnits(event.getProduct(), event.getUnits()));
    }

    /**
     * Listens to incoming events triggered by an item becoming available. These events are fired by the persistence layer.
     *
     * @param event Fired event
     */
    public void itemCreated(@Observes(during = AFTER_SUCCESS) @Created ItemEvent event) {
        getIndexes(event).forEach(index -> index.addUnits(event.getProduct(), event.getUnits()));
    }

    /**
     * Returns an indexed product with units available for all requested days
     *
     * @param from    Date from
     * @param to      Date to
     * @param product Product id
     * @return An IndexedProduct object representing product availability for the specified dates
     */
    public IndexedProduct getAvailableProduct(LocalDate from, LocalDate to, String product) {
        return getIndexes(from, to).stream()
                .map(index -> index.getById(product))
                .min(Comparator.comparing(indexed -> indexed.getUnits().get()))
                .orElseGet(() -> new IndexedProduct(product));
    }

    /**
     * Shifts the availability cache one day forward
     */
    void shift() {
        // add one day
        LocalDate today = LocalDate.now();
        LocalDate nextDay = today.plusDays(INDEXED_DAYS - 1);
        productDAO.getActiveProducts().forEach(product -> load(product, nextDay, nextDay));

        // removes old days
        availabilityCache.keySet().stream().filter(today::isAfter).forEach(availabilityCache::remove);
    }

    private Collection<AvailableProductIndex> getIndexes(ItemEvent event) {
        return event.isDated() ? getIndexes(event.getFrom(), event.getTo()) : availabilityCache.values();
    }

    private List<AvailableProductIndex> getIndexes(LocalDate from, LocalDate to) {
        return Stream.iterate(from, d -> d.plusDays(1))
                .limit(ChronoUnit.DAYS.between(from, to) + 1)
                .map(date -> availabilityCache.computeIfAbsent(date, k -> new AvailableProductIndex()))
                .collect(Collectors.toList());
    }

    private void load(Product product, LocalDate from, LocalDate to) {
        List<Item> items = itemDAO.getAvailableItems(product.getId());
        Set<String> itemIds = items.stream().map(Item::getId).collect(Collectors.toSet());
        Integer totalItems = itemIds.size();
        LocalDate max = from.minusDays(1);

        do {
            LocalDate min = max.plusDays(1);
            max = min(min.plusDays(RESERVATION_BATCH_SIZE), to);

            Map<LocalDate, Integer> reservationsByDay = getReservationsByDay(itemIds, from, to);

            Stream.iterate(min, d -> d.plusDays(1))
                    .limit(ChronoUnit.DAYS.between(min, max) + 1)
                    .forEach(date -> {
                        int available = totalItems - reservationsByDay.getOrDefault(date, 0);

                        if (available > 0) {
                            AvailableProductIndex index = availabilityCache.computeIfAbsent(date, k -> new AvailableProductIndex());

                            index.addUnits(product.getId(), available);
                        }
                    });
        } while (!max.isEqual(to));
    }

    private Map<LocalDate, Integer> getReservationsByDay(Set<String> ids, LocalDate from, LocalDate to) {
        Map<LocalDate, Integer> result = new HashMap<>();

        for (Reservation reservation : reservationDAO.findReservations(ids, from, to)) {
            LocalDate key = reservation.getDate();

            result.put(key, result.getOrDefault(key, 0) + 1);
        }
        return result;
    }

    private LocalDate min(LocalDate date1, LocalDate date2) {
        return date1.isBefore(date2) ? date1 : date2;
    }

    /**
     * Class that represents a product index and is intended to contain the number of available product units on any given day
     */
    private static class AvailableProductIndex {

        private final Map<String, IndexedProduct> productsById = new ConcurrentHashMap<>();

        public IndexedProduct getById(String id) {
            return productsById.computeIfAbsent(id, IndexedProduct::new);
        }

        public void addUnits(String product, int amount) {
            modify(product, amount);
        }

        public void removeUnits(String product, int amount) {
            modify(product, -amount);
        }

        private void modify(String product, int amount) {
            IndexedProduct indexed = getById(product);

            if (indexed.getUnits().addAndGet(amount) <= 0) {
                productsById.remove(product);
            }
        }
    }
}
