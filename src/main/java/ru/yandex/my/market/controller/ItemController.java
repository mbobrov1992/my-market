package ru.yandex.my.market.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yandex.my.market.model.dto.ItemCountDto;
import ru.yandex.my.market.model.enums.CartItemAction;
import ru.yandex.my.market.service.ItemService;
import ru.yandex.my.market.service.PriceService;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static ru.yandex.my.market.util.ListUtil.chunkWithPadding;

@RequiredArgsConstructor
@Controller
public class ItemController {

    private final ItemService itemService;
    private final PriceService priceService;

    @GetMapping("/")
    public String redirect() {
        return "redirect:/items";
    }

    @GetMapping("/items")
    public String getItems(
            Model model,
            @RequestParam(value = "search", defaultValue = "") String search,
            @RequestParam(value = "pageNumber", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "5") int pageSize,
            @RequestParam(value = "sort", defaultValue = "NO") ItemSortType sortType
    ) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortType.getSort());
        Page<ItemCountDto> itemPage = itemService.getItems(search, pageable);

        model.addAttribute("search", search);
        model.addAttribute("paging", itemPage);
        model.addAttribute("sort", sortType.name());
        model.addAttribute("items", chunkWithPadding(itemPage.get().toList(), 3, ItemCountDto.MOCK));

        return "items";
    }

    @PostMapping("/items")
    public String updateCartItemCount(
            RedirectAttributes redirect,
            @RequestParam(value = "id") Long itemId,
            @RequestParam(value = "action") CartItemAction action,
            @RequestParam(value = "search", defaultValue = "") String search,
            @RequestParam(value = "pageNumber", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "5") int pageSize,
            @RequestParam(value = "sort", defaultValue = "NO") ItemSortType sortType
    ) {
        itemService.updateCartItemCount(itemId, action);

        redirect.addAttribute("search", search);
        redirect.addAttribute("pageNumber", pageNumber);
        redirect.addAttribute("pageSize", pageSize);
        redirect.addAttribute("sort", sortType.name());

        return "redirect:/items";
    }

    @GetMapping("/items/{id}")
    public String getItem(
            Model model,
            @PathVariable(value = "id") Long id
    ) {
        ItemCountDto item = itemService.getItem(id);

        model.addAttribute("item", item);

        return "item";
    }

    @PostMapping("/items/{id}")
    public String updateCartItemCount(
            @PathVariable(value = "id") Long itemId,
            @RequestParam(value = "action") CartItemAction action
    ) {
        itemService.updateCartItemCount(itemId, action);

        return "redirect:/items/" + itemId;
    }

    @GetMapping("/cart/items")
    public String getCartItems(
            Model model
    ) {
        List<ItemCountDto> items = itemService.getCartItems();
        BigDecimal totalPrice = priceService.calculatePrice(items);

        model.addAttribute("items", items);
        model.addAttribute("total", totalPrice);

        return "cart";
    }

    @RequiredArgsConstructor
    public enum ItemSortType {
        NO(null),
        ALPHA("title"),
        PRICE("price");

        private final String fieldName;

        public Sort getSort() {
            return this == NO
                    ? Sort.unsorted()
                    : Sort.by(ASC, fieldName);
        }
    }
}
