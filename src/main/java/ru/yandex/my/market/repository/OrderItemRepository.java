package ru.yandex.my.market.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.yandex.my.market.model.entity.OrderItemEnt;

import java.util.Collection;

@Repository
public interface OrderItemRepository extends R2dbcRepository<OrderItemEnt, Long> {

    Flux<OrderItemEnt> findAllByOrderIdIn(Collection<Long> orderIds);
}
