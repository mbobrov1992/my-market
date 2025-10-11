package ru.yandex.my.market.model.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("cart_item")
public class CartItemEnt extends AuditableEntity {

    @Id
    @Column("id")
    private long id;

    @Column("item_id")
    private long itemId;

    @Column("count")
    private Integer count;
}
