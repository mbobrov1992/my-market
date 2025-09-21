package ru.yandex.my.market.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.my.market.model.entity.CartItemEnt;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends CrudRepository<CartItemEnt, Long> {

    List<CartItemEnt> findAllByItemIdIn(List<Long> itemIds);

    Optional<CartItemEnt> findByItemId(Long itemId);

    @Transactional
    @Modifying
    @Query("UPDATE CartItemEnt ci SET ci.count = ci.count + :delta WHERE ci.item.id = :itemId")
    void updateCartItemCount(@Param("itemId") Long itemId, @Param("delta") Integer delta);
}
