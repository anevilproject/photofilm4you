package edu.uoc.pdp.core.exception;

public class RentPenalizationException extends Exception {

    private static final long serialVersionUID = 2679992471466396656L;

    public RentPenalizationException(String rentId) {
        super("Could not close the penalization for rent " + rentId);
    }
}
