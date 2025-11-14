package ru.yandex.practicum.market.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.core.api.PaymentApi;
import ru.yandex.practicum.market.core.exception.PaymentException;
import ru.yandex.practicum.market.core.model.dto.*;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PaymentServiceTest {

    private static final String USER = "user";

    @Mock
    private PaymentApi paymentApi;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        paymentService = new PaymentService(paymentApi);
    }

    @Test
    void getBalance_ShouldReturnBalanceResponse() {
        BalanceResponse expectedBalance = new BalanceResponse();
        expectedBalance.setBalance(new BigDecimal("1500.50"));

        when(paymentApi.getBalance(USER))
                .thenReturn(Mono.just(expectedBalance));

        BalanceResponse actualBalance = paymentService.getBalance(USER).block();
        assertThat(actualBalance).isNotNull();
        assertThat(actualBalance.getBalance()).isEqualTo(expectedBalance.getBalance());

        verify(paymentApi).getBalance(USER);
    }

    @Test
    void pay_WhenSuccess_ShouldNotThrowException() {
        final String userId = "1";

        PaymentRequest request = new PaymentRequest();
        request.setUserId(userId);
        request.setAmount(new BigDecimal("500"));

        PaymentResponse successResponse = new PaymentResponse();
        successResponse.setUserId(userId);
        successResponse.setStatus(PaymentStatus.SUCCESS);
        successResponse.setTransactionId(UUID.randomUUID());
        successResponse.setMessage("Оплата выполнена успешно");

        when(paymentApi.pay(any(PaymentRequest.class))).thenReturn(Mono.just(successResponse));

        PaymentResponse response = paymentService.pay(request).block();
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(successResponse.getUserId());
        assertThat(response.getStatus()).isEqualTo(successResponse.getStatus());
        assertThat(response.getTransactionId()).isEqualTo(successResponse.getTransactionId());
        assertThat(response.getMessage()).isEqualTo(successResponse.getMessage());

        verify(paymentApi).pay(request);
    }

    @Test
    void pay_WhenFailed_ShouldThrowException() {
        final String userId = "1";

        PaymentRequest request = new PaymentRequest();
        request.setUserId(userId);
        request.setAmount(new BigDecimal("500"));

        PaymentResponse failResponse = new PaymentResponse();
        failResponse.setUserId(userId);
        failResponse.setStatus(PaymentStatus.FAILED);
        failResponse.setTransactionId(UUID.randomUUID());
        failResponse.setMessage("Недостаточно средств");

        when(paymentApi.pay(any(PaymentRequest.class))).thenReturn(Mono.just(failResponse));

        assertThatException()
                .isThrownBy(() -> paymentService.pay(request).block())
                .withMessage(failResponse.getMessage())
                .isExactlyInstanceOf(PaymentException.class);

        verify(paymentApi).pay(request);
    }
}
