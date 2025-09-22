package ru.yandex.my.market.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.my.market.mapper.ItemMapper;
import ru.yandex.my.market.model.dto.ItemCountDto;
import ru.yandex.my.market.model.entity.CartItemEnt;
import ru.yandex.my.market.model.entity.ItemEnt;
import ru.yandex.my.market.model.enums.CartItemAction;
import ru.yandex.my.market.repository.CartItemRepository;
import ru.yandex.my.market.repository.ItemRepository;

import java.util.*;

import static ru.yandex.my.market.model.enums.CartItemAction.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class ItemService {

    private final ItemRepository itemRepo;
    private final CartItemRepository cartItemRepo;
    private final ItemMapper itemMapper;

    @Transactional(readOnly = true)
    public Page<ItemCountDto> getItems(String search, Pageable pageable) {
        log.info("Получаем товары по строке: \"{}\"", search);

        Page<ItemEnt> itemPage = itemRepo.findAllByTitleIsContainingIgnoreCaseOrDescriptionIsContainingIgnoreCase(
                search, search, pageable
        );

        List<Long> itemIds = itemPage.stream()
                .map(ItemEnt::getId)
                .toList();
        Map<Long, Integer> cartItemCount = getCartItemCount(itemIds);
        return itemPage
                .map(itemMapper::toDto)
                .map(dto -> {
                    Integer count = cartItemCount.get(dto.id());
                    return new ItemCountDto(dto, count);
                });
    }

    private Map<Long, Integer> getCartItemCount(List<Long> itemIds) {
        if (itemIds.isEmpty()) return Map.of();

        log.info("Получаем количество товаров с ids: {} в корзине", itemIds);

        List<CartItemEnt> cartItems = cartItemRepo.findAllByItemIdIn(itemIds);

        Map<Long, Integer> cartItemCount = new HashMap<>();

        for (CartItemEnt cartItem : cartItems) {
            cartItemCount.put(cartItem.getItem().getId(), cartItem.getCount());
        }

        for (Long itemId : itemIds) {
            if (!cartItemCount.containsKey(itemId)) {
                cartItemCount.put(itemId, 0);
            }
        }

        return cartItemCount;
    }

    @Transactional
    public void updateCartItemCount(Long itemId, CartItemAction action) {
        Optional<CartItemEnt> cartItemOpt = cartItemRepo.findByItemId(itemId);

        if (cartItemOpt.isPresent()) {
            CartItemEnt cartItem = cartItemOpt.get();

            if (action == DELETE || cartItem.getCount() == 1 && action == MINUS) {
                log.info("Удаляем товар c id {} из корзины", itemId);
                cartItemRepo.delete(cartItem);
            } else {
                int delta = action == PLUS ? 1 : -1;
                log.info("Изменяем количество товара c id {} в корзине на дельту: {}", itemId, delta);
                cartItemRepo.updateCartItemCount(itemId, delta);
            }

        } else if (action == PLUS) {
            log.info("Добавляем товар c id {} в корзину", itemId);
            ItemEnt item = itemRepo.getReferenceById(itemId);

            CartItemEnt cartItem = new CartItemEnt();
            cartItem.setItem(item);
            cartItem.setCount(1);
            cartItemRepo.save(cartItem);
        }
    }

    @Transactional(readOnly = true)
    public ItemCountDto getItem(Long id) {
        log.info("Получаем товар c id {}", id);
        return itemRepo.findById(id)
                .map(itemMapper::toDto)
                .map(itemDto -> {
                    Map<Long, Integer> cartItemCount = getCartItemCount(List.of(id));
                    Integer count = cartItemCount.get(id);
                    return new ItemCountDto(itemDto, count);
                })
                .orElseThrow();
    }

    public List<ItemCountDto> getCartItems() {
        log.info("Получаем товары из корзины");

        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        return cartItemRepo.findAll(sort)
                .stream()
                .map(cartItem -> new ItemCountDto(
                        itemMapper.toDto(cartItem.getItem()),
                        cartItem.getCount()
                ))
                .toList();
    }
}
