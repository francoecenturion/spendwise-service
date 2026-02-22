package com.spendwise.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "ISSUING_ENTITY")
@Data
@EqualsAndHashCode(callSuper = true)
public class IssuingEntity extends BaseEntity {

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "ENABLED")
    private Boolean enabled;

}
