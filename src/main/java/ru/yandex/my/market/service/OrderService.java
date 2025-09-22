package ru.yandex.my.market.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.yandex.my.market.mapper.OrderMapper;
import ru.yandex.my.market.model.dto.OrderDto;
import ru.yandex.my.market.repository.OrderRepository;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepo;
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
}
