package ru.yandex.my.market.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.my.market.model.dto.ItemCountDto;
import ru.yandex.my.market.service.ItemService;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static ru.yandex.my.market.util.ListUtil.chunkWithPadding;

@RequiredArgsConstructor
@Controller
public class ItemController {

    private final ItemService itemService;

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
