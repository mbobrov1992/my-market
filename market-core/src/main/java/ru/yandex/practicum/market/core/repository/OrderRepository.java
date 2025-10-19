package ru.yandex.practicum.market.core.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.market.core.model.entity.OrderEnt;

@Repository
public interface OrderRepository extends R2dbcRepository<OrderEnt, Long> {
}
