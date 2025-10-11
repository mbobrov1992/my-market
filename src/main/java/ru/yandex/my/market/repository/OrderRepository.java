package ru.yandex.my.market.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.my.market.model.entity.OrderEnt;

@Repository
public interface OrderRepository extends R2dbcRepository<OrderEnt, Long> {
}
