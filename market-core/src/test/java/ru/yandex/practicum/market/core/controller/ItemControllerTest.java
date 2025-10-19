package ru.yandex.practicum.market.core.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.core.model.dto.CartItemDto;
import ru.yandex.practicum.market.core.model.dto.ItemSortType;
import ru.yandex.practicum.market.core.model.enums.CartItemAction;
import ru.yandex.practicum.market.core.service.CartItemService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WebFluxTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CartItemService cartItemService;

    @Test
    void testRedirectRoot() {
        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/items");
    }

    @Test
    void testGetItems() {
        final String search = "test";
        final int pageNumber = 1;
        final int pageSize = 2;
        final ItemSortType sortType = ItemSortType.ALPHA;

        CartItemDto mockItem = CartItemDto.MOCK;
        Page<CartItemDto> page = new PageImpl<>(List.of(mockItem));
        when(cartItemService.getItems(anyString(), any(PageRequest.class)))
                .thenReturn(Mono.just(page));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/items")
                        .queryParam("search", search)
                        .queryParam("pageNumber", String.valueOf(pageNumber))
                        .queryParam("pageSize", String.valueOf(pageSize))
                        .queryParam("sort", sortType.name())
                        .build())
                .exchange()
                .expectStatus().isOk();

        verify(cartItemService, times(1))
                .getItems(search, PageRequest.of(pageNumber, pageSize, sortType.getSort()));
    }

    @Test
    void testUpdateCartItemCountFromItemsView() {
        final long itemId = 1L;
        final CartItemAction action = CartItemAction.MINUS;

        when(cartItemService.updateCartItemCount(itemId, action))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/items")
                .body(BodyInserters.fromFormData("id", String.valueOf(itemId))
                        .with("action", action.name())
                        .with("search", "test")
                        .with("pageNumber", String.valueOf(1))
                        .with("pageSize", String.valueOf(2))
                        .with("sort", ItemSortType.ALPHA.name()))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/items");

        verify(cartItemService, times(1))
                .updateCartItemCount(itemId, action);
    }

    @Test
    void testGetItem() {
        final long itemId = 1L;

        when(cartItemService.getItem(itemId))
                .thenReturn(Mono.just(CartItemDto.MOCK));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/items/{id}")
                        .build(itemId))
                .exchange()
                .expectStatus().isOk();

        verify(cartItemService, times(1))
                .getItem(itemId);
    }

    @Test
    void testUpdateCartItemCountFromItemView() {
        final long itemId = 1L;
        final CartItemAction action = CartItemAction.MINUS;

        when(cartItemService.updateCartItemCount(itemId, action))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/items/{id}")
                        .build(itemId))
                .body(BodyInserters.fromFormData("action", action.name()))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/items/" + itemId);

        verify(cartItemService, times(1))
                .updateCartItemCount(itemId, action);
    }
}
