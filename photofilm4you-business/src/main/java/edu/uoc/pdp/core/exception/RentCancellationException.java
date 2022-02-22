package edu.uoc.pdp.core.exception;

public class RentCancellationException extends Exception {

    private static final long serialVersionUID = 2679992471466396656L;

    public RentCancellationException(String rentId) {
        super("Rent " + rentId + " could not be cancelled.");
    }
}
