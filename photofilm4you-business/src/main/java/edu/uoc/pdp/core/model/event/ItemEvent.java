package edu.uoc.pdp.core.model.event;

import edu.uoc.pdp.db.entity.Product;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Event fired when product availability changes on an item basis.
 *
 * The following qualifiers will be used to annotate what the event represents.
 *  - {@link edu.uoc.pdp.core.annotation.Created} New item available
 *  - {@link edu.uoc.pdp.core.annotation.Removed} An item is no longer available
 */
public class ItemEvent implements Serializable {

    private static final long serialVersionUID = 7376109297614235258L;

    private final String product;
    private final LocalDate from;
    private final LocalDate to;
    private final int units;

    public ItemEvent(Product product) {
        this(product.getId(), null, null, 1);
    }

    public ItemEvent(String product, LocalDate from, LocalDate to, int units) {
        this.product = product;
        this.from = from;
        this.to = to;
        this.units = units;
    }

    public int getUnits() {
        return units;
    }

    public boolean isDated() {
        return from != null && to != null;
    }

    public String getProduct() {
        return product;
    }

    public LocalDate getFrom() {
        return from;
    }

    public LocalDate getTo() {
        return to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ItemEvent itemEvent = (ItemEvent) o;
        return units == itemEvent.units &&
                Objects.equals(product, itemEvent.product) &&
                Objects.equals(from, itemEvent.from) &&
                Objects.equals(to, itemEvent.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(product, from, to, units);
    }

    @Override
    public String toString() {
        return "ItemEvent{" +
                "product='" + product + '\'' +
                ", from=" + from +
                ", to=" + to +
                ", units=" + units +
                '}';
    }
}
