package com.spendwise.model;

import com.spendwise.enums.PaymentMethodType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "RECOMMENDED_PAYMENT_METHOD")
@Data
@EqualsAndHashCode(callSuper = true)
public class RecommendedPaymentMethod extends BaseEntity {

    @Column(name = "NAME")
    private String name;

    @Column(name = "IMAGE_URL", length = 500)
    private String imageUrl;

    @Enumerated
    @Column(name = "PAYMENT_METHOD_TYPE")
    private PaymentMethodType paymentMethodType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECOMMENDED_ENTITY_ID")
    private RecommendedEntity entity;

    @Column(name = "DISPLAY_ORDER")
    private Integer displayOrder;

}
