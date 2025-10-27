package ru.yandex.practicum.payment.exception;

public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(String userId) {
        super("Не найден пользователь с id: " + userId);
    }
}
