package edu.uoc.pdp.core.model.availability;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class IndexedProduct {

    private final String productId;
    private final AtomicInteger units = new AtomicInteger(0);

    public IndexedProduct(String productId) {
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }

    public AtomicInteger getUnits() {
        return units;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        return productId.equals(((IndexedProduct) other).getProductId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }
}
