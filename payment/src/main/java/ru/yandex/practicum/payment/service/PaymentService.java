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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class PaymentService {

    private final Map<String, BigDecimal> userBalance;

    @Value("${balance.initial-value}")
    private BigDecimal initialBalance;

    public PaymentService() {
        this.userBalance = new ConcurrentHashMap<>();
    }

    public Mono<BalanceResponse> getBalance(String userId) {
        log.info("Получаем баланс счета пользователя с id: {}", userId);

        checkIfUserExists(userId);

        return Mono.just(new BalanceResponse()
                .userId(userId)
                .balance(userBalance.get(userId)));
    }

    public Mono<PaymentResponse> pay(Mono<PaymentRequest> request) {
        return request.flatMap(req -> Mono.fromCallable(() -> processPayment(req)));
    }

    private PaymentResponse processPayment(PaymentRequest request) {
        log.info("Выполняем оплату пользователем с id: {} на сумму: {}", request.getUserId(), request.getAmount());

        checkIfUserExists(request.getUserId());

        PaymentResponse response = new PaymentResponse()
                .transactionId(UUID.randomUUID())
                .userId(request.getUserId());

        try {
            BigDecimal balance = subtractAmount(request.getUserId(), request.getAmount());

            log.info("Произведена оплата пользователем с id: {} на сумму: {}", response.getUserId(), request.getAmount());

            return response.status(PaymentStatus.SUCCESS)
                    .message("Оплата выполнена успешно")
                    .newBalance(balance);
        } catch (PaymentException ex) {
            log.error("Ошибка оплаты: {}", ex.getMessage());
            return response.status(PaymentStatus.FAILED)
                    .message(ex.getMessage())
                    .newBalance(userBalance.get(request.getUserId()));
        }
    }

    private void checkIfUserExists(String userId) {
        if (userId == null || userId.isBlank()) {
            log.error("Не найден пользователь с id: {}", userId);
            throw new UserNotFoundException(userId);
        }

        userBalance.computeIfAbsent(userId, k -> initialBalance);
    }

    private BigDecimal subtractAmount(String userId, BigDecimal amount) {
        return userBalance.compute(userId, (k, currentBalance) -> {
            if (currentBalance == null || currentBalance.compareTo(amount) < 0) {
                throw new PaymentException("Недостаточно средств на счете пользователя с id: " + userId);
            }
            return currentBalance.subtract(amount);
        });
    }
}
