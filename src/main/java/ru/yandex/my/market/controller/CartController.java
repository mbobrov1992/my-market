package ru.yandex.my.market.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.my.market.model.dto.CartItemDto;
import ru.yandex.my.market.model.enums.CartItemAction;
import ru.yandex.my.market.service.CartItemService;
import ru.yandex.my.market.service.PriceService;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class CartController {

    private final CartItemService cartItemService;
    private final PriceService priceService;

    @GetMapping("/cart/items")
    public String getCartItems(
            Model model
    ) {
        List<CartItemDto> items = cartItemService.getCartItems();
        BigDecimal totalPrice = priceService.calculatePrice(items);

        model.addAttribute("items", items);
        model.addAttribute("total", totalPrice);

        return "cart";
    }

    @PostMapping("/cart/items")
    public String updateCartItemCountFromCartView(
            @RequestParam(value = "id") Long itemId,
            @RequestParam(value = "action") CartItemAction action
    ) {
        cartItemService.updateCartItemCount(itemId, action);

        return "redirect:/cart/items";
    }
}
