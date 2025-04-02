package ru.store.springbooks.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String username) {
        super(String.format("Пользователь с именем '%s' уже существует", username));
    }
}