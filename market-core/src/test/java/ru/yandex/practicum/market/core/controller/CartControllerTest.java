package ru.yandex.practicum.market.core.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.core.model.dto.CartItemDto;
import ru.yandex.practicum.market.core.model.dto.CartViewDto;
import ru.yandex.practicum.market.core.model.dto.PaymentInfo;
import ru.yandex.practicum.market.core.model.enums.CartItemAction;
import ru.yandex.practicum.market.core.service.CartItemService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;

@WebFluxTest(CartController.class)
class CartControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CartItemService cartItemService;

    @Test
    void testGetCartItems() {
        CartItemDto mockItem = CartItemDto.MOCK;
        List<CartItemDto> mockItems = List.of(mockItem);
        BigDecimal mockTotalPrice = BigDecimal.valueOf(100);
        PaymentInfo mockPaymentInfo = new PaymentInfo(null, true);

        when(cartItemService.getCartView()).thenReturn(Mono.just(new CartViewDto(mockItems, mockTotalPrice, mockPaymentInfo)));

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk();

        verify(cartItemService, times(1))
                .getCartView();
    }

    @Test
    void testUpdateCartItemCountFromCartView() {
        final long itemId = 1L;
        final CartItemAction action = CartItemAction.MINUS;

        when(cartItemService.updateCartItemCount(itemId, action))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/cart/items")
                .body(BodyInserters.fromFormData("id", String.valueOf(itemId))
                        .with("action", action.name()))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/cart/items");

        verify(cartItemService, times(1))
                .updateCartItemCount(itemId, action);
    }
}
