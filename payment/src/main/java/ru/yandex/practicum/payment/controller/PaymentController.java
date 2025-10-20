package ru.yandex.practicum.payment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.api.BalanceApi;
import ru.yandex.practicum.payment.api.PaymentApi;
import ru.yandex.practicum.payment.model.BalanceResponse;
import ru.yandex.practicum.payment.model.PaymentRequest;
import ru.yandex.practicum.payment.model.PaymentResponse;
import ru.yandex.practicum.payment.service.PaymentService;

@RequiredArgsConstructor
@RestController
public class PaymentController implements BalanceApi, PaymentApi {

    private final PaymentService paymentService;

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalance(String userId, ServerWebExchange exchange) {
        return paymentService.getBalance(userId)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> pay(Mono<PaymentRequest> paymentPostRequest, ServerWebExchange exchange) {
        return paymentService.pay(paymentPostRequest)
                .map(ResponseEntity::ok);
    }
}
