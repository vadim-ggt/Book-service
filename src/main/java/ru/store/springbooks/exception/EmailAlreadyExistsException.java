package ru.store.springbooks.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super(String.format("Пользователь с email '%s' уже существует", email));
    }
}