package ru.yandex.my.market.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
}
