package org.codegenerator.exceptions;

public class CallWrapperException extends RuntimeException {
    public CallWrapperException(Exception e) {
        super(e);
    }
}
