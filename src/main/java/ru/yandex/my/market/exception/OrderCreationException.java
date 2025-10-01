package ru.yandex.my.market.exception;

public class OrderCreationException extends RuntimeException {

    public OrderCreationException(String message) {
        super("Невозможно создать заказ. " + message);
    }
}
