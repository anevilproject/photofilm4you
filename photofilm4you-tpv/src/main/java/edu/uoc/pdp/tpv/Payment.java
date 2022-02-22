package edu.uoc.pdp.tpv;

import java.math.BigDecimal;

public class Payment {

    private final BigDecimal amount;
    private final String identifier;

    public Payment(BigDecimal amount, String identifier) {
        this.amount = amount;
        this.identifier = identifier;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getIdentifier() {
        return identifier;
    }
}
