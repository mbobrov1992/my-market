package ru.yandex.practicum.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.exception.PaymentException;
import ru.yandex.practicum.payment.exception.UserNotFoundException;
import ru.yandex.practicum.payment.model.BalanceResponse;
import ru.yandex.practicum.payment.model.PaymentRequest;
import ru.yandex.practicum.payment.model.PaymentResponse;
import ru.yandex.practicum.payment.model.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class PaymentService {

    private static final String NOT_FOUND_USER_ID = "UNKNOWN";

    private final AtomicReference<BigDecimal> balance;

    public PaymentService(@Value("${balance.initial-value}") BigDecimal initialBalance) {
        this.balance = new AtomicReference<>(initialBalance);
    }

    public Mono<BalanceResponse> getBalance(String userId) {
        log.info("Получаем баланс счета");

        checkIfUserExists(userId);

        return Mono.just(new BalanceResponse()
                .userId(userId)
                .balance(balance.get()));
    }

    public Mono<PaymentResponse> pay(Mono<PaymentRequest> request) {
        return request.flatMap(req -> Mono.fromCallable(() -> processPayment(req)));
    }

    private PaymentResponse processPayment(PaymentRequest request) {
        log.info("Выполняем оплату пользователем {} на сумму {}", request.getUserId(), request.getAmount());

        checkIfUserExists(request.getUserId());

        PaymentResponse response = new PaymentResponse()
                .transactionId(UUID.randomUUID())
                .userId(request.getUserId());

        try {
            BigDecimal balance = subtractAmount(request.getAmount());

            log.info("Произведена оплата пользователем {} на сумму {}", response.getUserId(), request.getAmount());

            return response.status(PaymentStatus.SUCCESS)
                    .message("Оплата выполнена успешно")
                    .newBalance(balance);
        } catch (PaymentException ex) {
            log.error("Ошибка оплаты: {}", ex.getMessage());
            return response.status(PaymentStatus.FAILED)
                    .message(ex.getMessage())
                    .newBalance(balance.get());
        }
    }

    private void checkIfUserExists(String userId) {
        if (NOT_FOUND_USER_ID.equals(userId)) {
            log.error("Не найден пользователь с id: {}", userId);
            throw new UserNotFoundException(userId);
        }
    }

    private BigDecimal subtractAmount(BigDecimal amount) {
        return balance.updateAndGet(current -> {
            if (current.subtract(amount).signum() == -1) {
                throw new PaymentException("Недостаточно средств на счете");
            }
            return current.subtract(amount);
        });
    }
}
