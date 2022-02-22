package edu.uoc.pdp.core.exception;

public class NoShoppingCartException extends Exception {

    private static final long serialVersionUID = -7404450137637011231L;

    public NoShoppingCartException() {
        super("User has no active shopping cart!");
    }
}
