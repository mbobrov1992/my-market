package ru.yandex.my.market.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.my.market.config.TestcontainersConfig;
import ru.yandex.my.market.mapper.ItemMapper;
import ru.yandex.my.market.model.dto.CartItemDto;
import ru.yandex.my.market.model.dto.ItemDto;
import ru.yandex.my.market.model.entity.ItemEnt;
import ru.yandex.my.market.repository.ItemRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;

@DataJpaTest
@Import(TestcontainersConfig.class)
class ItemServiceTest {

    @Autowired
    private ItemRepository itemRepo;

    @MockitoBean
    private ItemMapper itemMapper;

    @MockitoBean
    private CartService cartService;

    @Test
    void testGetItems() {
        ItemEnt item = new ItemEnt();
        item.setTitle("Тестовый товар");
        item.setDescription("Описание");
        item.setPrice(BigDecimal.ONE);
        item = itemRepo.save(item);

        ItemDto dto = new ItemDto(1, null, null, null, null);
        Mockito.when(itemMapper.toDto(any(ItemEnt.class))).thenReturn(dto);
        Mockito.when(cartService.getCartItemCount(anyList())).thenReturn(Map.of(item.getId(), 10));

        ItemService itemService = new ItemService(itemRepo, itemMapper, cartService);

        Page<CartItemDto> resultPage = itemService.getItems("Тестовый", PageRequest.of(0, 10));

        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getContent()).isNotEmpty();
        assertThat(resultPage.getContent().getFirst().count()).isEqualTo(10);
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
        Mockito.when(cartService.getCartItemCount(List.of(item.getId()))).thenReturn(Map.of(item.getId(), 5));

        ItemService itemService = new ItemService(itemRepo, itemMapper, cartService);

        CartItemDto result = itemService.getItem(item.getId());

        assertThat(result).isNotNull();
        assertThat(result.count()).isEqualTo(5);
    }
}
