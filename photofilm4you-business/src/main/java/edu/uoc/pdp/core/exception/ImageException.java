package edu.uoc.pdp.core.exception;

public class ImageException extends Exception {

    private static final long serialVersionUID = 7268819199639480333L;

    public ImageException() {
        super("La imatge proporcionada no és vàlida");
    }
}
