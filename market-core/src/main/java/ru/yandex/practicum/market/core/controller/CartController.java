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

import java.security.Principal;

@RequiredArgsConstructor
@Controller
public class CartController {

    private final CartItemService cartItemService;

    @GetMapping("/cart/items")
    public Mono<Rendering> getCartItems(Mono<Principal> principal) {
        return principal.map(Principal::getName)
                .flatMap(username -> cartItemService.getCartView(username)
                        .map(cartView -> Rendering.view("cart")
                                .modelAttribute("items", cartView.cartItems())
                                .modelAttribute("total", cartView.totalPrice())
                                .modelAttribute("paymentInfo", cartView.paymentInfo())
                                .build()));
    }

    @PostMapping("/cart/items")
    public Mono<Rendering> updateCartItemCountFromCartView(
            Mono<Principal> principal,
            @ModelAttribute CartItemUpdateForm updateRequest
    ) {
        return principal.map(Principal::getName)
                .flatMap(username -> cartItemService.updateCartItemCount(username, updateRequest.id(), updateRequest.action())
                        .then(Mono.just(Rendering.redirectTo("/cart/items")
                                .build())));
    }
}
