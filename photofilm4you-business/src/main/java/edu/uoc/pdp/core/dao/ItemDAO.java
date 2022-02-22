package edu.uoc.pdp.core.dao;

import edu.uoc.pdp.core.annotation.Created;
import edu.uoc.pdp.core.model.event.ItemEvent;
import edu.uoc.pdp.db.entity.Item;
import edu.uoc.pdp.db.entity.Item_;
import edu.uoc.pdp.db.entity.Product;
import edu.uoc.pdp.db.entity.Product_;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static edu.uoc.pdp.db.entity.ItemStatus.BROKEN;
import static edu.uoc.pdp.db.entity.ItemStatus.OPERATIONAL;

@Singleton
public class ItemDAO extends BaseDAO<Item> {

    @Inject
    @Created
    private Event<ItemEvent> itemCreatedEvent;
    @Inject
    private ProductDAO productDAO;

    /**
     * Persists an item to the database. Also checks for changes that will impact item availability and fires
     * the necessary events accordingly.
     *
     * @param item Item to be persisted
     * @return The persisted item instance
     */
    @Override
    @Transactional
    public Item save(Item item) {
        notifyChanges(item);

        return super.save(item);
    }

    /**
     * Retrieves all non-deleted and operational items that belong to a product
     *
     * @param productId Product the items belong to
     * @return A list containing all rentable items for a product
     */
    public List<Item> getAvailableItems(String productId) {
        return findList((item, builder) -> availableItemPredicate(item, builder, productId));
    }

    /**
     * Retrieves all items that belong to a product
     *
     * @param productId Product the items belong to
     * @return A list containing all items for a product
     */
    public List<Item> getAllItems(String productId) {
        return findList((item, builder) -> builder.equal(item.get(Item_.PRODUCT).get(Product_.ID), productId));
    }

    /**
     * Returns the number of active and operational items of a product
     *
     * @param productId Product identifier
     * @return Number of available units
     */
    public long countAvailableItems(String productId) {
        return count((item, builder) -> availableItemPredicate(item, builder, productId));
    }

    /**
     * Returns the number of non-deleted items of a product
     *
     * @param productId Product identifier
     * @return Number of active units
     */
    public long countActiveItems(String productId) {
        return count((item, builder) -> activeItemPredicate(item, builder, productId));
    }

    /**
     * Checks if an item with different id and the same serial_number and product_id already exists
     *
     * @param item Item to check
     * @return {@code true} if an item like the specified one already exists, {@code false} otherwise
     */
    public boolean existsItem(Item item) {
        return exists((root, builder) -> builder.and(
                builder.notEqual(root.get(Item_.ID), item.getId() == null ? "" : item.getId()),
                builder.equal(root.get(Item_.SERIAL_NUMBER), item.getSerialNumber()),
                builder.equal(root.get(Item_.PRODUCT).get(Product_.ID), item.getProductId())
        ));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Order> defaultOrder(Root<Item> root, CriteriaBuilder builder) {
        return Arrays.asList(
                builder.asc(root.get(Item_.PRODUCT).get(Product_.ID)),
                builder.asc(root.get(Item_.SERIAL_NUMBER)));
    }

    private Predicate availableItemPredicate(Root<Item> item, CriteriaBuilder builder, String productId) {
        return builder.and(
                activeItemPredicate(item, builder, productId),
                builder.equal(item.get(Item_.STATUS), OPERATIONAL));
    }

    private Predicate activeItemPredicate(Root<Item> item, CriteriaBuilder builder, String productId) {
        return builder.and(
                builder.equal(item.get(Item_.PRODUCT).get(Product_.ID), productId),
                builder.isNull(item.get(Item_.DELETED)));
    }

    private void notifyChanges(Item item) {
        if (item.getDeleted() != null) {
            return;
        }
        Product product = productDAO.getById(item.getProduct().getId());

        if (item.getId() != null) {
            Item old = getById(item.getId());

            if (old.getStatus() == BROKEN && item.getStatus() == OPERATIONAL
                    || old.getDeleted() != null && item.getDeleted() == null) {
                itemCreatedEvent.fire(new ItemEvent(product));
            } else if (old.getStatus() == OPERATIONAL && item.getStatus() == BROKEN) {
                itemRemovedEvent.fire(new ItemEvent(product));
            } else if (!Objects.equals(product.getId(), old.getProduct().getId())) {
                itemCreatedEvent.fire(new ItemEvent(product));
                itemRemovedEvent.fire(new ItemEvent(old.getProduct()));
            }
        } else if (item.getStatus() == OPERATIONAL) {
            itemCreatedEvent.fire(new ItemEvent(product));
        }
    }
}
