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

    public static final String MOCK_USER_ID = "MOCK_USER_ID";

    private final PaymentApi paymentApi;

    public Mono<BalanceResponse> getBalance() {
        return paymentApi.getBalance(MOCK_USER_ID);
    }

    public Mono<PaymentResponse> pay(PaymentRequest request) {
        request.setUserId(MOCK_USER_ID);

        log.info("Выполняем оплату пользователем {} на сумму {}", request.getUserId(), request.getAmount());

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
