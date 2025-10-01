package ru.yandex.my.market.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.my.market.config.TestcontainersConfig;
import ru.yandex.my.market.mapper.ItemMapper;
import ru.yandex.my.market.model.dto.CartItemDto;
import ru.yandex.my.market.model.dto.ItemDto;
import ru.yandex.my.market.model.entity.CartItemEnt;
import ru.yandex.my.market.model.entity.ItemEnt;
import ru.yandex.my.market.model.enums.CartItemAction;
import ru.yandex.my.market.repository.CartItemRepository;
import ru.yandex.my.market.repository.ItemRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@DataJpaTest
@Import(TestcontainersConfig.class)
public class CartItemServiceTest {

    @Autowired
    private CartItemRepository cartItemRepo;

    @Autowired
    private ItemRepository itemRepo;

    @MockitoBean
    private ItemMapper itemMapper;

    private CartItemService cartItemService;

    @BeforeEach
    void setUp() {
        cartItemService = new CartItemService(cartItemRepo, itemRepo, itemMapper);
    }

    @Test
    void testGetItems() {
        ItemEnt item = new ItemEnt();
        item.setTitle("Тестовый товар");
        item.setDescription("Описание");
        item.setPrice(BigDecimal.ONE);
        itemRepo.save(item);

        ItemDto dto = new ItemDto(1, null, null, null, null);
        Mockito.when(itemMapper.toDto(any(ItemEnt.class))).thenReturn(dto);

        Page<CartItemDto> resultPage = cartItemService.getItems("Тестовый", PageRequest.of(0, 10));

        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getContent()).isNotEmpty();
        assertThat(resultPage.getContent().getFirst().count()).isEqualTo(0);
    }

    @Test
    void testGetItem() {
        ItemEnt item = new ItemEnt();
        item.setTitle("Тестовый товар");
        item.setDescription("Описание");
        item.setPrice(BigDecimal.ONE);
        item = itemRepo.save(item);

        ItemDto dto = new ItemDto(1, null, null, null, null);
        Mockito.when(itemMapper.toDto(item)).thenReturn(dto);

        CartItemDto result = cartItemService.getItem(item.getId());

        assertThat(result).isNotNull();
        assertThat(result.count()).isEqualTo(0);
    }

    @Test
    void testGetCartItemCount_withItems_returnsCountIncludingZeroForMissing() {
        ItemEnt item1 = getMockItem();
        ItemEnt item2 = getMockItem();
        itemRepo.saveAll(List.of(item1, item2));

        CartItemEnt cartItem1 = new CartItemEnt();
        cartItem1.setItem(item1);
        cartItem1.setCount(3);
        cartItemRepo.save(cartItem1);

        List<Long> itemIds = List.of(item1.getId(), item2.getId());
        Map<Long, Integer> counts = cartItemService.getCartItemCount(itemIds);

        assertThat(counts).hasSize(2);
        assertThat(counts.get(item1.getId())).isEqualTo(3);
        assertThat(counts.get(item2.getId())).isEqualTo(0);
    }

    @Test
    void testUpdateCartItemCount_addNewItem() {
        ItemEnt item = getMockItem();
        itemRepo.save(item);

        cartItemService.updateCartItemCount(item.getId(), CartItemAction.PLUS);

        List<CartItemEnt> cartItems = cartItemRepo.findAll(Sort.by(Sort.Direction.ASC, "id"));
        assertThat(cartItems).hasSize(1);
        assertThat(cartItems.getFirst().getItem().getId()).isEqualTo(item.getId());
        assertThat(cartItems.getFirst().getCount()).isEqualTo(1);
    }

    @Test
    void testUpdateCartItemCount_deleteItem() {
        ItemEnt item = getMockItem();
        itemRepo.save(item);

        CartItemEnt cartItem = new CartItemEnt();
        cartItem.setItem(item);
        cartItem.setCount(2);
        cartItemRepo.save(cartItem);

        cartItemService.updateCartItemCount(item.getId(), CartItemAction.DELETE);
        assertThat(cartItemRepo.findByItemId(item.getId())).isEmpty();
    }

    @Test
    void testDeleteCartItems() {
        ItemEnt item = getMockItem();
        itemRepo.save(item);

        CartItemEnt cartItem = new CartItemEnt();
        cartItem.setItem(item);
        cartItem.setCount(5);
        cartItemRepo.save(cartItem);

        cartItemService.deleteCartItems();

        assertThat(cartItemRepo.findAll()).isEmpty();
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
