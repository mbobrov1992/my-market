package ru.yandex.my.market.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.my.market.AbstractIntegrationTest;
import ru.yandex.my.market.model.dto.OrderDto;
import ru.yandex.my.market.model.entity.CartItemEnt;
import ru.yandex.my.market.model.entity.ItemEnt;
import ru.yandex.my.market.model.entity.OrderEnt;
import ru.yandex.my.market.repository.CartItemRepository;
import ru.yandex.my.market.repository.ItemRepository;
import ru.yandex.my.market.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderServiceTest extends AbstractIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private ItemRepository itemRepo;

    @Autowired
    private CartItemRepository cartItemRepo;

    @BeforeEach
    void setup() {
        ItemEnt item1 = getMockItem();
        itemRepo.save(item1).block();

        CartItemEnt cartItem1 = new CartItemEnt();
        cartItem1.setItemId(item1.getId());
        cartItem1.setCount(5);
        cartItemRepo.save(cartItem1).block();

        ItemEnt item2 = getMockItem();
        itemRepo.save(item2).block();

        CartItemEnt cartItem2 = new CartItemEnt();
        cartItem2.setItemId(item2.getId());
        cartItem2.setCount(5);
        cartItemRepo.save(cartItem2).block();

        orderRepo.deleteAll().block();
    }

    @Test
    void testCreateOrder() {
        Long orderId = orderService.createOrder().block();

        assertThat(orderId).isNotNull();

        List<OrderEnt> orderEnts = orderRepo.findAll().collectList().block();
        assertThat(orderEnts).isNotEmpty();
        assertThat(orderEnts.getFirst().getId()).isEqualTo(orderId);

        assertThat(cartItemRepo.findAll().collectList().block()).isEmpty();
    }

    @Test
    void testGetOrders_andGetOrder() {
        Long orderId = orderService.createOrder().block();

        List<OrderDto> orderDtos = orderService.getOrders().collectList().block();
        assertThat(orderDtos).hasSize(1);
        assertThat(orderDtos.getFirst().id()).isEqualTo(orderId);

        OrderDto orderDto = orderService.getOrder(orderId).block();
        assertThat(orderDto).isNotNull();
        assertThat(orderDto.id()).isEqualTo(orderId);
    }

    private ItemEnt getMockItem() {
        Random random = new Random();

        ItemEnt item = new ItemEnt();
        item.setTitle("Тестовый товар " + random.nextInt());
        item.setDescription("Описание товара " + random.nextInt());
        item.setPrice(BigDecimal.valueOf(1 + random.nextInt(10000)));
        item.setImagePath("http://random.test.server");
        return item;
    }
}
