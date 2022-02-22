package edu.uoc.pdp.core.exception;

public class ProductNotAvailableException extends Exception {

    private static final long serialVersionUID = 5346059666745862044L;

    public ProductNotAvailableException(String product) {
        super("Product " + product + " is not currently available!");
    }
}
