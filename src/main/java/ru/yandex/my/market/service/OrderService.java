package ru.yandex.my.market.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.my.market.mapper.OrderMapper;
import ru.yandex.my.market.model.dto.CartItemDto;
import ru.yandex.my.market.model.dto.OrderDto;
import ru.yandex.my.market.model.entity.OrderEnt;
import ru.yandex.my.market.model.entity.OrderItemEnt;
import ru.yandex.my.market.repository.ItemRepository;
import ru.yandex.my.market.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final ItemRepository itemRepo;
    private final CartItemService cartItemService;
    private final PriceService priceService;
    private final OrderMapper orderMapper;

    public List<OrderDto> getOrders() {
        log.info("Получаем список заказов");

        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        return orderRepo.findAll(sort)
                .stream()
                .map(orderMapper::toDto)
                .toList();
    }

    public OrderDto getOrder(Long id) {
        log.info("Получаем заказ с id: {}", id);

        return orderRepo.findById(id)
                .map(orderMapper::toDto)
                .orElseThrow();
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderDto createOrder() {
        log.info("Начинаем процесс создания заказа");

        List<CartItemDto> cartItems = cartItemService.getCartItems();

        cartItemService.deleteCartItems();

        OrderEnt order = createOrder(cartItems);
        orderRepo.save(order);

        log.info("Создан заказ с id: {}", order.getId());

        return orderMapper.toDto(order);
    }

    private OrderEnt createOrder(List<CartItemDto> cartItems) {
        OrderEnt order = new OrderEnt();

        order.setItems(createOrderItems(order, cartItems));
        order.setTotalPrice(calculatePrice(cartItems));

        return order;
    }

    private List<OrderItemEnt> createOrderItems(OrderEnt order, List<CartItemDto> cartItems) {
        List<OrderItemEnt> orderItems = new ArrayList<>();

        cartItems.forEach(cartItem -> {
            OrderItemEnt orderItem = createOrderItem(order, cartItem);
            orderItems.add(orderItem);
        });

        return orderItems;
    }

    private OrderItemEnt createOrderItem(OrderEnt order, CartItemDto cartItem) {
        OrderItemEnt orderItem = new OrderItemEnt();
        orderItem.setOrder(order);
        orderItem.setItem(itemRepo.getReferenceById(cartItem.id()));
        orderItem.setCount(cartItem.count());
        orderItem.setPrice(cartItem.price());
        return orderItem;
    }

    private BigDecimal calculatePrice(List<CartItemDto> cartItems) {
        return priceService.calculatePrice(cartItems);
    }
}
