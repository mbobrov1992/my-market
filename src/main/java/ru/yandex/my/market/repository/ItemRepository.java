package ru.yandex.my.market.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.my.market.model.entity.ItemEnt;

@Repository
public interface ItemRepository extends JpaRepository<ItemEnt, Long> {

    Page<ItemEnt> findAllByTitleIsContainingIgnoreCaseOrDescriptionIsContainingIgnoreCase(
            String title, String description, Pageable pageable
    );
}
