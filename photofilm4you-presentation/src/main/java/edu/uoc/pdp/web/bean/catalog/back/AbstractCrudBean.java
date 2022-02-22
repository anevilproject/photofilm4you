package edu.uoc.pdp.web.bean.catalog.back;

import com.google.common.reflect.TypeToken;
import edu.uoc.pdp.db.entity.Deletable;
import edu.uoc.pdp.db.entity.Identifiable;
import edu.uoc.pdp.web.bean.BaseBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractCrudBean<T extends Identifiable> extends BaseBean {

    private static final long serialVersionUID = 3117382242056267641L;

    private static final Logger log = LoggerFactory.getLogger(AbstractCrudBean.class);

    private List<T> allItems;
    @Valid
    private T item;
    private String id;

    protected abstract T fetch(String id);

    protected abstract void delete(String id) throws Exception;

    protected abstract void persist(T item) throws Exception;

    protected abstract List<T> fetchAll();

    protected abstract String getListView();

    /**
     * Fetches all elements of an entity and caches it locally for the view
     *
     * @return List with all elements
     */
    public List<T> getAllItems() {
        if (allItems == null) {
            allItems = fetchAll();
        }
        return allItems;
    }

    /**
     * Retrieves an element by the specified or creates a new object if an id was not specified
     *
     * @return Entity instance
     * @throws RuntimeException If a new instance could not be created when needed
     */
    @SuppressWarnings({"unchecked", "UnstableApiUsage"})
    public T getItem() {
        if (item == null) {
            if (id == null) {
                try {
                    item = ((Class<T>) new TypeToken<T>(getClass()) {
                    }.getRawType()).newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else {
                item = fetch(id);
            }
        }
        return item;
    }

    /**
     * Deletes an entity
     */
    public void delete() {
        try {
            delete(id);
            addGlobalSuccessMessage("S'ha esborrat l'element correctament");
        } catch (Exception e) {
            addExceptionMessage(e);
        }
        redirect();
    }

    /**
     * Persists the changes made to an entity
     */
    public void save() {
        if (StringUtils.isNotBlank(id)) { // Allows request scoped beans to be used
            item.setId(id);
        }
        try {
            persist(item);
            addGlobalSuccessMessage("S'ha guardat l'element correctament");
            redirect(getListView());
        } catch (Exception e) {
            addExceptionMessage(e);
            redirect(true);
        }
    }

    /**
     * Changes the status of a Deletable entity which was deleted back to active
     */
    public void reactivate() {
        T entity = fetch(id);

        try {
            if (entity instanceof Deletable) {
                Deletable deletable = (Deletable) entity;
                Deletable upstream = deletable.getUpstream();

                if (upstream == null || upstream.getDeleted() == null) {
                    deletable.setDeleted(null);
                    persist(entity);
                    addGlobalSuccessMessage("S'ha reactivat l'element correctament");
                } else {
                    addGlobalErrorMessage("L'element no es pot reactivar ja que el "
                            + upstream.getClass().getSimpleName() + " associat es troba inactiu");
                }
            } else {
                addGlobalErrorMessage("Aquesta entitat no es pot reactivar");
            }
        } catch (Exception e) {
            addExceptionMessage(e);
        }
        redirect();
    }

    public String getRowClass() {
        return getRowClass(getAllItems());
    }

    protected <S> String getRowClass(List<S> items) {
        return items.stream().map(this::getItemRowClass).collect(Collectors.joining(","));
    }

    private <S> String getItemRowClass(S item) {
        return item instanceof Deletable && ((Deletable) item).getDeleted() != null ? "row-deleted" : "row-active";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private void addExceptionMessage(Exception e) {
        log.error(e.getMessage(), e);

        String message = e.getMessage();

        if (StringUtils.isBlank(message)) {
            message = "There was an unexpected error: " + e.getClass().getName();
        }
        addGlobalErrorMessage(message);
    }
}
