package ru.yandex.my.market.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yandex.my.market.model.dto.OrderDto;
import ru.yandex.my.market.service.OrderService;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/orders")
    public String getOrders(
            Model model
    ) {
        List<OrderDto> orders = orderService.getOrders();

        model.addAttribute("orders", orders);

        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String getOrder(
            Model model,
            @PathVariable(value = "id") Long id,
            @RequestParam(value = "newOrder", defaultValue = "false") boolean isNew
    ) {
        OrderDto order = orderService.getOrder(id);

        model.addAttribute("order", order);
        model.addAttribute("newOrder", isNew);

        return "order";
    }

    @PostMapping("/buy")
    public String buy(
            RedirectAttributes redirect
    ) {
        OrderDto order = orderService.createOrder();

        redirect.addAttribute("newOrder", true);

        return "redirect:/orders/" + order.id();
    }
}
