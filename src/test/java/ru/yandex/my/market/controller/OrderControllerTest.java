package ru.yandex.my.market.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.yandex.my.market.model.dto.OrderDto;
import ru.yandex.my.market.service.OrderService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void testGetOrders() throws Exception {
        OrderDto mockOrder = new OrderDto(1L, List.of(), BigDecimal.ONE);
        List<OrderDto> mockOrders = List.of(mockOrder);

        when(orderService.getOrders()).thenReturn(mockOrders);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attribute("orders", mockOrders));
    }

    @Test
    void testGetOrderWithNewOrderParam() throws Exception {
        OrderDto mockOrder = new OrderDto(1L, List.of(), BigDecimal.ONE);

        when(orderService.getOrder(1L)).thenReturn(mockOrder);

        mockMvc.perform(get("/orders/1")
                        .param("newOrder", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attribute("order", mockOrder))
                .andExpect(model().attribute("newOrder", true));
    }

    @Test
    void testGetOrderWithoutNewOrderParam() throws Exception {
        OrderDto mockOrder = new OrderDto(1L, List.of(), BigDecimal.ONE);

        when(orderService.getOrder(1L)).thenReturn(mockOrder);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attribute("order", mockOrder))
                .andExpect(model().attribute("newOrder", false));
    }

    @Test
    void testBuy() throws Exception {
        OrderDto mockOrder = new OrderDto(1L, List.of(), BigDecimal.ONE);

        when(orderService.createOrder()).thenReturn(mockOrder);

        ResultActions result = mockMvc.perform(post("/buy"));

        result.andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/" + mockOrder.id() + "?newOrder=true"));
    }
}
