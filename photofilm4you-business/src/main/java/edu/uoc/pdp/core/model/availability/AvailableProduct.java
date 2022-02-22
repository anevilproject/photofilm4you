package edu.uoc.pdp.core.model.availability;

import edu.uoc.pdp.db.entity.Product;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class AvailableProduct implements Serializable {

    private static final long serialVersionUID = 5676269961285452634L;

    private final Product product;
    private final int units;
    private final int days;

    public AvailableProduct(Product product, int units, int days) {
        this.product = product;
        this.units = units;
        this.days = days;
    }

    public Product getProduct() {
        return product;
    }

    public int getUnits() {
        return units;
    }

    public String getId() {
        return product.getId();
    }

    public BigDecimal getDailyPriceFormatted() {
        return getDailyPrice().setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getDailyPrice() {
        return product.getDailyPrice().multiply(BigDecimal.valueOf(units));
    }

    public BigDecimal getTotalPrice() {
        return getDailyPrice().multiply(BigDecimal.valueOf(days));
    }

    public BigDecimal getTotalPriceFormatted() {
        return getTotalPrice().setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AvailableProduct product1 = (AvailableProduct) o;
        return units == product1.units &&
                days == product1.days &&
                Objects.equals(product, product1.product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(product, units, days);
    }

    @Override
    public String toString() {
        return "AvailableProduct{" +
                "product=" + (product == null ? null : product.getId()) +
                ", units=" + units +
                ", days=" + days +
                '}';
    }
}
