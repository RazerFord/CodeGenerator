package org.codegenerator.exceptions;

public class InvariantCheckingException extends RuntimeException {
    public InvariantCheckingException() {
    }

    public InvariantCheckingException(String message) {
        super(message);
    }
}
