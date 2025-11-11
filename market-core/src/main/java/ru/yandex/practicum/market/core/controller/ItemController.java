package ru.yandex.practicum.market.core.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.core.model.dto.CartItemUpdateForm;
import ru.yandex.practicum.market.core.model.dto.ItemFilterForm;
import ru.yandex.practicum.market.core.model.dto.CartItemDto;
import ru.yandex.practicum.market.core.service.CartItemService;

import static ru.yandex.practicum.market.core.util.ListUtil.chunkWithPadding;

@RequiredArgsConstructor
@Controller
public class ItemController {

    private final CartItemService cartItemService;

    @GetMapping("/")
    public Mono<Rendering> redirect() {
        return Mono.just(Rendering.redirectTo("/items")
                .build());
    }

    @GetMapping("/items")
    public Mono<Rendering> getItems(
            @ModelAttribute ItemFilterForm filter
    ) {
        Pageable pageable = PageRequest.of(
                filter.getPageNumber(),
                filter.getPageSize(),
                filter.getSort().getSort()
        );

        return cartItemService.getItems(filter.getSearch(), pageable)
                .map(itemPage -> Rendering.view("items")
                        .modelAttribute("search", filter.getSearch())
                        .modelAttribute("paging", itemPage)
                        .modelAttribute("sort", filter.getSort().name())
                        .modelAttribute("items", chunkWithPadding(itemPage.get().toList(), 3, CartItemDto.MOCK))
                        .build());
    }

    @PostMapping("/items")
    public Mono<Rendering> updateCartItemCountFromItemsView(
            @ModelAttribute CartItemUpdateForm updateRequest,
            @ModelAttribute ItemFilterForm filter
    ) {
        return cartItemService.updateCartItemCount(updateRequest.id(), updateRequest.action())
                .then(Mono.just(Rendering.redirectTo("/items")
                        .modelAttribute("search", filter.getSearch())
                        .modelAttribute("pageNumber", filter.getPageNumber())
                        .modelAttribute("pageSize", filter.getPageSize())
                        .modelAttribute("sort", filter.getSort().name())
                        .build()));
    }

    @GetMapping("/items/{id}")
    public Mono<Rendering> getItem(
            @PathVariable(value = "id") Long id
    ) {
        return cartItemService.getItem(id)
                .map(item -> Rendering.view("item")
                        .modelAttribute("item", item)
                        .build());
    }

    @PostMapping("/items/{id}")
    public Mono<Rendering> updateCartItemCountFromItemView(
            @PathVariable(value = "id") Long itemId,
            @ModelAttribute CartItemUpdateForm updateRequest
    ) {
        return cartItemService.updateCartItemCount(itemId, updateRequest.action())
                .then(Mono.just(Rendering.redirectTo("/items/" + itemId)
                        .build()));
    }
}
