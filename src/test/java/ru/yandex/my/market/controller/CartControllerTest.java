package ru.yandex.my.market.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.my.market.model.dto.CartItemDto;
import ru.yandex.my.market.model.enums.CartItemAction;
import ru.yandex.my.market.service.CartItemService;
import ru.yandex.my.market.service.PriceService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;

@WebFluxTest(CartController.class)
class CartControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CartItemService cartItemService;

    @MockitoBean
    private PriceService priceService;

    @Test
    void testGetCartItems() {
        CartItemDto mockItem = CartItemDto.MOCK;
        List<CartItemDto> mockItems = List.of(mockItem);
        BigDecimal mockTotalPrice = BigDecimal.valueOf(100);

        when(cartItemService.getCartItems()).thenReturn(Flux.fromIterable(mockItems));
        when(priceService.calculatePrice(mockItems)).thenReturn(mockTotalPrice);

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk();

        verify(cartItemService, times(1))
                .getCartItems();
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
