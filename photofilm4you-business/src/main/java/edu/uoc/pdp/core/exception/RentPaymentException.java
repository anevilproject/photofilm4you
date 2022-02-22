package edu.uoc.pdp.core.exception;

public class RentPaymentException extends Exception {

    private static final long serialVersionUID = -8289741619389643554L;

    public RentPaymentException(String rentId) {
        super("Rent " + rentId + " is already paid for!");
    }
}
