package ru.yandex.practicum.market.core.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.core.model.entity.ItemEnt;

@Repository
public interface ItemRepository extends R2dbcRepository<ItemEnt, Long> {

    Flux<ItemEnt> findAllByTitleIsContainingIgnoreCaseOrDescriptionIsContainingIgnoreCase(
            String title, String description, Pageable pageable
    );

    Mono<Long> countAllByTitleIsContainingIgnoreCaseOrDescriptionIsContainingIgnoreCase(
            String title, String description
    );
}
