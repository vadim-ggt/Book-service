package ru.store.springbooks.exception;

public class InvalidAuthorNameException extends RuntimeException {
    public InvalidAuthorNameException(String message) {
        super("Invalid value in field: " + message + ". Author name should not contain numbers.");
    }
}
