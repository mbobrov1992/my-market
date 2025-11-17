package ru.yandex.practicum.market.core.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.core.model.entity.CartItemEnt;

import java.util.List;

@Repository
public interface CartItemRepository extends R2dbcRepository<CartItemEnt, Long> {

    Flux<CartItemEnt> findAllByCartId(Long cartId, Sort sort);

    Flux<CartItemEnt> findAllByCartIdAndItemIdIn(Long cartId, List<Long> itemIds);

    Mono<CartItemEnt> findByCartIdAndItemId(Long cartId, Long itemId);

    @Transactional
    @Modifying
    @Query("""
            UPDATE cart_item
            SET count = count + :delta,
            updated_at = CURRENT_TIMESTAMP
            WHERE cart_id = :cartId AND item_id = :itemId AND count + :delta > 0
            """)
    Mono<Long> updateCartItemCount(
            @Param("cartId") Long cartId,
            @Param("itemId") Long itemId,
            @Param("delta") Integer delta
    );

    Mono<Void> deleteAllByCartId(Long cartId);
}
