package ru.yandex.practicum.market.core.exception;

public class OrderCreationException extends RuntimeException {

    public OrderCreationException(String message) {
        super("Невозможно создать заказ. " + message);
    }
}
