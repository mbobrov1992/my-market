package ru.yandex.practicum.market.core.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.core.model.dto.OrderDto;
import ru.yandex.practicum.market.core.service.OrderService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;

@WebFluxTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private OrderService orderService;

    @Test
    void testGetOrders() {
        OrderDto mockOrder = new OrderDto(1L, List.of(), BigDecimal.ONE);
        List<OrderDto> mockOrders = List.of(mockOrder);

        when(orderService.getOrders()).thenReturn(Flux.fromIterable(mockOrders));

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk();

        verify(orderService, times(1))
                .getOrders();
    }

    @Test
    void testGetOrderWithNewOrderParam() {
        final long orderId = 1L;

        OrderDto mockOrder = new OrderDto(orderId, List.of(), BigDecimal.ONE);

        when(orderService.getOrder(orderId)).thenReturn(Mono.just(mockOrder));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/orders/{id}")
                        .queryParam("newOrder", true)
                        .build(orderId))
                .exchange()
                .expectStatus().isOk();

        verify(orderService, times(1))
                .getOrder(orderId);
    }

    @Test
    void testGetOrderWithoutNewOrderParam() {
        final long orderId = 1L;

        OrderDto mockOrder = new OrderDto(orderId, List.of(), BigDecimal.ONE);

        when(orderService.getOrder(orderId)).thenReturn(Mono.just(mockOrder));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/orders/{id}")
                        .build(orderId))
                .exchange()
                .expectStatus().isOk();

        verify(orderService, times(1))
                .getOrder(orderId);
    }

    @Test
    void testBuy() {
        final long orderId = 1L;

        when(orderService.createOrder()).thenReturn(Mono.just(orderId));

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/orders/" + orderId + "?newOrder=true");

        verify(orderService, times(1))
                .createOrder();
    }
}
