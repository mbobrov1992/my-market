package ru.yandex.practicum.market.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.core.api.PaymentApi;
import ru.yandex.practicum.market.core.exception.PaymentException;
import ru.yandex.practicum.market.core.model.dto.BalanceResponse;
import ru.yandex.practicum.market.core.model.dto.PaymentRequest;
import ru.yandex.practicum.market.core.model.dto.PaymentResponse;
import ru.yandex.practicum.market.core.model.dto.PaymentStatus;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentApi paymentApi;

    public Mono<BalanceResponse> getBalance(String userId) {
        log.debug("Получаем баланс счета пользователя с id: {}", userId);

        return paymentApi.getBalance(userId)
                .doOnError(ex ->
                        log.error("При получении баланса счета пользователя с id: {} возникла ошибка: {}",
                                userId, ex.getMessage())
                );
    }

    public Mono<PaymentResponse> pay(PaymentRequest request) {
        log.info("Выполняем оплату пользователем с id: {} на сумму {}", request.getUserId(), request.getAmount());

        return paymentApi.pay(request)
                .handle((response, sink) -> {
                    if (response.getStatus() != PaymentStatus.SUCCESS) {
                        sink.error(new PaymentException(response.getMessage()));
                        log.error("Ошибка оплаты заказа. Идентификатор транзакции: {}", response.getTransactionId());
                        return;
                    }

                    sink.next(response);
                });
    }
}
