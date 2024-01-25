package org.codegenerator.exceptions;

public class JacoDBException extends RuntimeException {
    public JacoDBException(Exception e) {
        super(e);
    }
}
