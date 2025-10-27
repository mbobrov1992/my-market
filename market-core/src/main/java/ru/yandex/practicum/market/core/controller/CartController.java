package ru.yandex.practicum.market.core.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.core.model.dto.CartItemUpdateForm;
import ru.yandex.practicum.market.core.service.CartItemService;

@RequiredArgsConstructor
@Controller
public class CartController {

    private final CartItemService cartItemService;

    @GetMapping("/cart/items")
    public Mono<Rendering> getCartItems() {
        return cartItemService.getCartView()
                .map(cartView -> Rendering.view("cart")
                        .modelAttribute("items", cartView.cartItems())
                        .modelAttribute("total", cartView.totalPrice())
                        .modelAttribute("paymentInfo", cartView.paymentInfo())
                        .build());
    }

    @PostMapping("/cart/items")
    public Mono<Rendering> updateCartItemCountFromCartView(
            @ModelAttribute CartItemUpdateForm updateRequest
    ) {
        return cartItemService.updateCartItemCount(updateRequest.id(), updateRequest.action())
                .then(Mono.just(Rendering.redirectTo("/cart/items")
                        .build()));
    }
}
