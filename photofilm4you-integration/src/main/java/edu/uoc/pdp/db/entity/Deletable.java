package edu.uoc.pdp.db.entity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Marks an entity as deletable. Delete operations regarding objects implementing this interface will not be entirely
 * removed, but marked as deleted instead in order to preserve original database relations.
 */
public interface Deletable {

    /**
     * Retrieves deletable object's deleted timestamp
     *
     * @return A {@link LocalDateTime} representing the moment the item was deleted. Might be {@code null}.
     */
    LocalDateTime getDeleted();

    /**
     * Marks an object as deleted
     *
     * @param deleted Deletion timestamp
     */
    void setDeleted(LocalDateTime deleted);

    /**
     * Retrieves associated entities that must be deleted when deleting the implementing object.
     *
     * @return A list of deleted objects
     */
    default List<? extends Deletable> getCascade() {
        return Collections.emptyList();
    }


    default Deletable getUpstream() {
        return null;
    }

}
