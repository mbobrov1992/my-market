package ru.yandex.practicum.market.core.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.core.model.entity.OrderEnt;

@Repository
public interface OrderRepository extends R2dbcRepository<OrderEnt, Long> {

    Flux<OrderEnt> findAllByUserId(Long userId, Sort sort);

    Mono<OrderEnt> findByUserIdAndUserOrderId(Long userId, Long userOrderId);

    Mono<OrderEnt> findTopByUserIdOrderByUserOrderIdDesc(Long userId);
}
