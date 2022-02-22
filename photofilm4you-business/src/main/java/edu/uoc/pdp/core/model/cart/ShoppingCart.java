package edu.uoc.pdp.core.model.cart;

import edu.uoc.pdp.core.model.availability.AvailableProduct;
import edu.uoc.pdp.db.entity.Product;
import edu.uoc.pdp.db.entity.Rentable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static edu.uoc.pdp.core.utils.DateUtils.format;

public class ShoppingCart implements Rentable, Serializable {

    private static final long serialVersionUID = -6998046434254721567L;

    private final LocalDateTime created = LocalDateTime.now();
    private final LocalDate from;
    private final LocalDate to;
    private final Map<String, AvailableProduct> items = new LinkedHashMap<>();

    public ShoppingCart(LocalDate from, LocalDate to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public LocalDate getFrom() {
        return from;
    }

    @Override
    public LocalDate getTo() {
        return to;
    }

    public String getFromAsString() {
        return format(from);
    }

    public String getToAsString() {
        return format(to);
    }

    public List<AvailableProduct> getItems() {
        return new ArrayList<>(items.values());
    }

    public int size() {
        return items.size();
    }

    public LocalDateTime getCreated() {
        return created;
    }

    @Override
    public BigDecimal getTotalPrice() {
        return items.values().stream()
                .map(AvailableProduct::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getDailyPrice() {
        return items.values().stream()
                .map(AvailableProduct::getDailyPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Adds the specified amount of units of a product to the cart
     *
     * @param product Product to be added
     * @param units   Number of units to be added
     */
    public void addItem(Product product, int units) {
        items.put(product.getId(), new AvailableProduct(product, getUnitsOfProduct(product.getId()) + units, getDuration()));
    }

    /**
     * Returns the number of units of a product that were added to the cart, 0 if the product is not present
     *
     * @param productId Product id
     * @return Number or units of the specified product in the cart
     */
    public int getUnitsOfProduct(String productId) {
        return items.containsKey(productId) ? items.get(productId).getUnits() : 0;
    }

    /**
     * Removes a product from the cart
     *
     * @param id Id of the product to remove
     */
    public void removeProduct(String id) {
        items.remove(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ShoppingCart that = (ShoppingCart) o;
        return Objects.equals(from, that.from) &&
                Objects.equals(to, that.to) &&
                Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, items);
    }
}
