package ru.yandex.practicum.market.core.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.core.service.OrderService;

@RequiredArgsConstructor
@Controller
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/orders")
    public Mono<Rendering> getOrders() {
        return orderService.getOrders()
                .collectList()
                .map(orders -> Rendering.view("orders")
                        .modelAttribute("orders", orders)
                        .build());
    }

    @GetMapping("/orders/{id}")
    public Mono<Rendering> getOrder(
            @PathVariable(value = "id") Long id,
            @RequestParam(value = "newOrder", defaultValue = "false") boolean isNew
    ) {
        return orderService.getOrder(id)
                .map(order -> Rendering.view("order")
                        .modelAttribute("order", order)
                        .modelAttribute("newOrder", isNew)
                        .build());
    }

    @PostMapping("/buy")
    public Mono<Rendering> buy() {
        return orderService.createOrder()
                .map(orderId -> Rendering.redirectTo("/orders/" + orderId + "?newOrder=true")
                        .build())
                .onErrorReturn(Rendering.redirectTo("/cart/items")
                        .build());
    }
}
