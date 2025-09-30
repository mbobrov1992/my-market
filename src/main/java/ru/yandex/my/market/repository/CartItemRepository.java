package ru.yandex.my.market.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.my.market.model.entity.CartItemEnt;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItemEnt, Long> {

    @EntityGraph(attributePaths = {"item"})
    List<CartItemEnt> findAll(Sort sort);

    List<CartItemEnt> findAllByItemIdIn(List<Long> itemIds);

    Optional<CartItemEnt> findByItemId(Long itemId);

    @Transactional
    @Modifying
    @Query("UPDATE CartItemEnt ci SET ci.count = ci.count + :delta WHERE ci.item.id = :itemId AND ci.count + :delta > 0")
    void updateCartItemCount(@Param("itemId") Long itemId, @Param("delta") Integer delta);
}
