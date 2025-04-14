package ru.store.springbooks.exception;


public class EmptyFieldException extends RuntimeException {
    public EmptyFieldException(String fieldName) {
        super(String.format("Поле '%s' не может быть пустым или null", fieldName));
    }
}
