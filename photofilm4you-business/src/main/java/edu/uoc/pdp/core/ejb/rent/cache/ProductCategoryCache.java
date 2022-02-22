package edu.uoc.pdp.core.ejb.rent.cache;

import edu.uoc.pdp.core.dao.CategoryDAO;
import edu.uoc.pdp.core.dao.ProductDAO;
import edu.uoc.pdp.core.model.event.CategoryIndexEvent;
import edu.uoc.pdp.db.entity.Category;
import edu.uoc.pdp.db.entity.Product;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Startup
@PermitAll
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class ProductCategoryCache {

    private static final Logger log = LoggerFactory.getLogger(ProductCategoryCache.class);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private Map<String, Map<String, Boolean>> cache;
    private Future<?> runningRebuild;

    @Inject
    private ProductDAO productDAO;
    @Inject
    private CategoryDAO categoryDAO;


    @PostConstruct
    public void load() {
        buildIndex();
    }

    /**
     * Queues an index rebuild. Should be invoked when categories or products change.
     *
     * @param event Fired event
     */
    public void rebuildIndex(@Observes(during = TransactionPhase.AFTER_SUCCESS) CategoryIndexEvent event) {
        if (runningRebuild != null && !runningRebuild.isDone()) {
            runningRebuild.cancel(true);
        }
        runningRebuild = executor.submit(this::buildIndex);
    }

    /**
     * Retrieves all product ids associated to a category or any of its children
     *
     * @param category Category to get the products from
     * @return A set containing the products of the specified category
     */
    public Set<String> getCategoryProducts(String category) {
        return getProductMap(cache, category).keySet();
    }

    /**
     * Retrieves all active product ids associated to a category or any of its children
     *
     * @param category Category to get the products from
     * @return A set containing the active products of the specified category
     */
    public Set<String> getActiveCategoryProducts(String category) {
        Stream<String> ids;

        if (StringUtils.isNotBlank(category)) {
            ids = getActiveProducts(getProductMap(cache, category));
        } else {
            ids = cache.values().stream().flatMap(this::getActiveProducts);
        }
        return ids.collect(Collectors.toSet());
    }

    private Stream<String> getActiveProducts(Map<String, Boolean> map) {
        return map.keySet().stream().filter(map::get);
    }

    private void buildIndex() {
        log.info("Started category index rebuild!");
        Map<String, Map<String, Boolean>> cache = new ConcurrentHashMap<>();
        List<Product> products = productDAO.getAll();

        for (Product product : products) {
            String id = product.getId();

            for (String category : getCategories(product)) {
                checkInterruption();

                getProductMap(cache, category).put(id, product.getDeleted() == null);
            }
        }
        this.cache = cache;
        log.info("Indexed {} products in {} categories", products.size(), cache.size());
    }

    private void checkInterruption() {
        if (Thread.interrupted()) {
            log.info("Category index rebuild interrupted!");

            throw new RuntimeException("Build interrupted");
        }
    }

    private Map<String, Boolean> getProductMap(Map<String, Map<String, Boolean>> cache, String category) {
        return cache.computeIfAbsent(category, k -> new ConcurrentHashMap<>());
    }

    private Set<String> getCategories(Product product) {
        Set<String> result = new HashSet<>();
        Category category = categoryDAO.getById(product.getCategory().getId());

        do {
            if (!result.add(category.getId())) {
                break;
            }
            if (category.getParentId() == null) {
                category = null;
            } else {
                category = categoryDAO.getById(category.getParentId());
            }
        } while (category != null);

        return result;
    }
}
