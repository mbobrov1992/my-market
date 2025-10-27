package ru.yandex.practicum.market.core.model.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Getter
@Setter
@Table("item")
public class ItemEnt extends AuditableEntity {

    @Id
    @Column("id")
    private long id;

    @Column("title")
    private String title;

    @Column("image_path")
    private String imagePath;

    @Column("description")
    private String description;

    @Column("price")
    private BigDecimal price;
}
