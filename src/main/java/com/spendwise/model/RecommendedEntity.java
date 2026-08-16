package com.spendwise.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "RECOMMENDED_ENTITY")
@Data
@EqualsAndHashCode(callSuper = true)
public class RecommendedEntity extends BaseEntity {

    @Column(name = "NAME")
    private String name;

    @Column(name = "ICON_URL", length = 500)
    private String iconUrl;

}
