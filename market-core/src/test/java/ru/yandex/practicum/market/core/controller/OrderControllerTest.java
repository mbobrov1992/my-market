package ru.yandex.practicum.market.core.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.core.config.TestSecurityConfig;
import ru.yandex.practicum.market.core.model.dto.OrderDto;
import ru.yandex.practicum.market.core.service.OrderService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;

@WebFluxTest(controllers = OrderController.class)
@Import(TestSecurityConfig.class)
class OrderControllerTest {

    private static final String USER = "user";

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private OrderService orderService;

    @WithAnonymousUser
    @Test
    void testGetOrdersUnauthenticated() {
        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/login");
    }

    @WithMockUser(username = USER)
    @Test
    void testGetOrders() {
        OrderDto mockOrder = new OrderDto(1L, List.of(), BigDecimal.ONE);
        List<OrderDto> mockOrders = List.of(mockOrder);

        when(orderService.getOrders(USER)).thenReturn(Flux.fromIterable(mockOrders));

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk();

        verify(orderService, times(1))
                .getOrders(USER);
    }

    @WithAnonymousUser
    @Test
    void testGetOrderUnauthenticated() {
        final long orderId = 1L;

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/orders/{id}")
                        .build(orderId))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/login");
    }

    @WithMockUser(username = USER)
    @Test
    void testGetOrderWithNewOrderParam() {
        final long orderId = 1L;

        OrderDto mockOrder = new OrderDto(orderId, List.of(), BigDecimal.ONE);

        when(orderService.getOrder(USER, orderId)).thenReturn(Mono.just(mockOrder));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/orders/{id}")
                        .queryParam("newOrder", true)
                        .build(orderId))
                .exchange()
                .expectStatus().isOk();

        verify(orderService, times(1))
                .getOrder(USER, orderId);
    }

    @WithMockUser(username = USER)
    @Test
    void testGetOrderWithoutNewOrderParam() {
        final long orderId = 1L;

        OrderDto mockOrder = new OrderDto(orderId, List.of(), BigDecimal.ONE);

        when(orderService.getOrder(USER, orderId)).thenReturn(Mono.just(mockOrder));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/orders/{id}")
                        .build(orderId))
                .exchange()
                .expectStatus().isOk();

        verify(orderService, times(1))
                .getOrder(USER, orderId);
    }

    @WithAnonymousUser
    @Test
    void testBuyUnauthenticated() {
        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/login");
    }

    @WithMockUser(username = USER)
    @Test
    void testBuy() {
        final long orderId = 1L;

        when(orderService.createOrder(USER)).thenReturn(Mono.just(orderId));

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/orders/" + orderId + "?newOrder=true");

        verify(orderService, times(1))
                .createOrder(USER);
    }
}
