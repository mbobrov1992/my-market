package ru.yandex.practicum.market.core.model.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("cart")
public class CartEnt extends AuditableEntity {

    @Id
    @Column("id")
    private long id;

    @Column("user_id")
    private long userId;
}
