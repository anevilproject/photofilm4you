package edu.uoc.pdp.core.dao;

import com.google.common.reflect.TypeToken;
import edu.uoc.pdp.core.annotation.Removed;
import edu.uoc.pdp.core.model.event.CategoryIndexEvent;
import edu.uoc.pdp.core.model.event.ItemEvent;
import edu.uoc.pdp.db.entity.Deletable;
import edu.uoc.pdp.db.entity.Identifiable;
import edu.uoc.pdp.db.entity.Item;
import edu.uoc.pdp.db.entity.Product;
import org.apache.commons.collections4.CollectionUtils;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Basic data access structure for simple CRUD operations
 */
public abstract class BaseDAO<T extends Identifiable> {

    protected final BiFunction<Root<T>, CriteriaBuilder, Predicate> NO_PREDICATE = (root, builder) -> null;
    protected final BiFunction<Root<T>, CriteriaBuilder, List<Order>> NO_ORDER = (root, builder) -> null;

    @PersistenceContext
    protected EntityManager entityManager;
    @Inject
    @Removed
    protected Event<ItemEvent> itemRemovedEvent;
    @Inject
    protected Event<CategoryIndexEvent> categoryIndexEvent;

    protected final Class<T> type;

    @SuppressWarnings({"unchecked", "UnstableApiUsage"})
    public BaseDAO() {
        type = (Class<T>) new TypeToken<T>(getClass()) {
        }.getRawType();
    }

    /**
     * Queries the database in order to find one element matching by primary key.
     *
     * @param id Element id
     * @return The object instance if an element was found, {@code null} otherwise
     */
    public T getById(String id) {
        T result = entityManager.find(type, id);

        if (result == null) {
            throw new EntityNotFoundException("Could not find " + type.getSimpleName() + " with id " + id);
        }
        return result;
    }

    /**
     * Retrieves all elements of an entity from the database.
     *
     * @return A list containing all the elements of the given type. Returns an empty list if there are no results.
     */
    public List<T> getAll() {
        return findList(NO_PREDICATE);
    }

    /**
     * Persists the status of an element into the database
     *
     * @param entity Entity to be persisted
     * @return The persisted instance of the element
     */
    @Transactional
    public T save(T entity) {
        T target = entity;

        if (entity.getId() != null && !entityManager.contains(entity)) {
            target = entityManager.merge(entity);
        }
        entityManager.persist(target);

        return target;
    }

    /**
     * Deletes an element from the database.
     * <p>
     * If the given type implements {@link Deletable} it will mark the element as deleted instead and will proceed to
     * cascade through the related entities using {@link Deletable#getCascade()} in order to mark those as deleted as well
     *
     * @param entity Element to be deleted
     */
    @Transactional
    public void delete(T entity) {
        if (entity instanceof Deletable) {
            markAsDeleted((Deletable) getById(entity.getId()));
        } else {
            entityManager.remove(entity);
        }
    }

    /**
     * Deletes an entity by id
     *
     * @param id Entity id
     * @throws EntityNotFoundException if there is no entity of the given type with the specified identifier
     */
    @Transactional
    public void delete(String id) {
        delete(getById(id));
    }

    /**
     * Checks if at least one element matching a predicate exists
     *
     * @param predicateBuilder Predicate function
     * @return {@code true} if there's at least one matching element, {@code false} otherwise
     */
    protected boolean exists(BiFunction<Root<T>, CriteriaBuilder, Predicate> predicateBuilder) {
        return count(predicateBuilder) > 0;
    }

    /**
     * Executes a count query with the given predicate
     *
     * @param predicateBuilder Predicate function
     * @return Number of elements matching the specified predicate
     */
    protected long count(BiFunction<Root<T>, CriteriaBuilder, Predicate> predicateBuilder) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
        Root<T> root = criteria.from(type);
        Predicate predicate = predicateBuilder.apply(root, builder);

        criteria = criteria.select(builder.count(root));

        if (predicate != null) {
            criteria = criteria.where(predicate);
        }
        return Optional.ofNullable(entityManager.createQuery(criteria).getSingleResult()).orElse(0L);
    }

    /**
     * Retrieves the elements of the given type matching the specified predicate
     *
     * @param predicateBuilder Function to build the required where clause
     * @return A list with the matching elements
     */
    protected List<T> findList(BiFunction<Root<T>, CriteriaBuilder, Predicate> predicateBuilder) {
        return findList(predicateBuilder, NO_ORDER);
    }

    /**
     * Retrieves the elements of the given type matching the specified predicate and sorted according to the specified order
     *
     * @param predicateBuilder Function to build the required where clause
     * @param orderBuilder     Function to build the required order by clause
     * @return A list with the matching elements sorted
     */
    protected List<T> findList(
            BiFunction<Root<T>, CriteriaBuilder, Predicate> predicateBuilder,
            BiFunction<Root<T>, CriteriaBuilder, List<Order>> orderBuilder) {
        return buildQuery(predicateBuilder, orderBuilder).getResultList();
    }

    /**
     * Queries for an element matching the specified predicate.
     *
     * @param predicateBuilder Function to build the required where clause
     * @return The matching element if any, wrapped in an {@link Optional}
     */
    protected T findOne(BiFunction<Root<T>, CriteriaBuilder, Predicate> predicateBuilder) {
        return buildQuery(predicateBuilder, (root, builder) -> Collections.emptyList()).getSingleResult();
    }

    /**
     * Builds a typed query object with the specified where and order by clauses
     *
     * @param predicateBuilder Function to build the required where clause
     * @param orderBuilder     Function to build the required order by clause
     * @return A TypedQuery<T> instance
     */
    protected TypedQuery<T> buildQuery(
            BiFunction<Root<T>, CriteriaBuilder, Predicate> predicateBuilder,
            BiFunction<Root<T>, CriteriaBuilder, List<Order>> orderBuilder) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> criteria = builder.createQuery(type);
        Root<T> root = criteria.from(type);
        Predicate predicate = predicateBuilder.apply(root, builder);
        List<Order> order = orderBuilder.apply(root, builder);
        List<Order> defaultOrder = defaultOrder(root, builder);

        criteria = criteria.select(root);

        if (predicate != null) {
            criteria = criteria.where(predicate);
        }
        if (order != null) {
            criteria = criteria.orderBy(order);
        } else if (defaultOrder != null) {
            criteria = criteria.orderBy(defaultOrder);
        }
        return entityManager.createQuery(criteria);
    }

    /**
     * Defines the default ordering when no special ordering is specified in a list query
     *
     * @param root    Root
     * @param builder Criteria builder
     * @return A nullable list with the orders to be used by default
     */
    protected List<Order> defaultOrder(Root<T> root, CriteriaBuilder builder) {
        return null;
    }

    private void markAsDeleted(Deletable entity) {
        List<? extends Deletable> cascade = entity.getCascade();

        if (CollectionUtils.isNotEmpty(cascade)) {
            cascade.forEach(this::markAsDeleted);
        }
        entity.setDeleted(LocalDateTime.now());
        entityManager.persist(entity);

        if (entity instanceof Item) {
            Item item = (Item) entity;
            Product product = item.getProduct();

            itemRemovedEvent.fire(new ItemEvent(product));
        } else if (entity instanceof Product) {
            categoryIndexEvent.fire(new CategoryIndexEvent());
        }
    }
}
