package ru.yandex.my.market.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
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

@Transactional
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
        itemRepo.save(item1);

        CartItemEnt cartItem1 = new CartItemEnt();
        cartItem1.setItem(item1);
        cartItem1.setCount(5);
        cartItemRepo.save(cartItem1);

        ItemEnt item2 = getMockItem();
        itemRepo.save(item2);

        CartItemEnt cartItem2 = new CartItemEnt();
        cartItem2.setItem(item2);
        cartItem2.setCount(5);
        cartItemRepo.save(cartItem2);

        orderRepo.deleteAll();
    }

    @Test
    void testCreateOrder() {
        OrderDto orderDto = orderService.createOrder();

        assertThat(orderDto).isNotNull();
        assertThat(orderDto.totalSum()).isGreaterThan(BigDecimal.ZERO);
        assertThat(orderDto.items()).isNotEmpty();

        List<OrderEnt> orderEnts = orderRepo.findAll();
        assertThat(orderEnts).isNotEmpty();
        assertThat(orderEnts.get(0).getTotalPrice()).isEqualTo(orderDto.totalSum());

        assertThat(cartItemRepo.findAll()).isEmpty();
    }

    @Test
    void testGetOrders_andGetOrder() {
        OrderDto orderDto = orderService.createOrder();

        List<OrderDto> orderDtos = orderService.getOrders();
        assertThat(orderDtos).hasSize(1);

        assertThat(orderService.getOrder(orderDto.id())).isEqualTo(orderDto);
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
