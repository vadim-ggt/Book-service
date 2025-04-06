package ru.store.springbooks.exception;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException() {
        super("Пароль должен содержать как минимум 8 символов и включать "
                + "хотя бы одну цифру и одну заглавную букву.");
    }
}