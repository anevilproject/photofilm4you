package edu.uoc.pdp.db.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

public interface Rentable {

    LocalDate getFrom();

    LocalDate getTo();

    default BigDecimal getTotalPrice() {
        return BigDecimal.ZERO;
    }

    default int getDuration() {
        LocalDate from = getFrom();
        LocalDate to = getTo();

        if (from == null || to == null) {
            return 0;
        }
        return (int) DAYS.between(from, to) + 1;
    }
}
