package ru.yandex.my.market.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.my.market.mapper.ItemMapper;
import ru.yandex.my.market.model.dto.CartItemDto;
import ru.yandex.my.market.model.entity.CartItemEnt;
import ru.yandex.my.market.model.entity.ItemEnt;
import ru.yandex.my.market.model.enums.CartItemAction;
import ru.yandex.my.market.repository.CartItemRepository;
import ru.yandex.my.market.repository.ItemRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.yandex.my.market.model.enums.CartItemAction.*;
import static ru.yandex.my.market.model.enums.CartItemAction.PLUS;

@Slf4j
@RequiredArgsConstructor
@Service
public class CartService {

    private final CartItemRepository cartItemRepo;
    private final ItemRepository itemRepo;
    private final ItemMapper itemMapper;

    public Map<Long, Integer> getCartItemCount(List<Long> itemIds) {
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

    public List<CartItemDto> getCartItems() {
        log.info("Получаем товары из корзины");

        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        return cartItemRepo.findAll(sort)
                .stream()
                .map(cartItem -> new CartItemDto(
                        itemMapper.toDto(cartItem.getItem()),
                        cartItem.getCount()
                ))
                .toList();
    }

    public void deleteCartItems() {
        log.info("Удаляем товары из корзины");

        cartItemRepo.deleteAllInBatch();
    }
}
