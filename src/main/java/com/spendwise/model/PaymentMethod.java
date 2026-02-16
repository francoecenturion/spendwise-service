package com.spendwise.model;

import com.spendwise.enums.PaymentMethodType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "PAYMENT_METHOD")
public class PaymentMethod extends BaseEntity {

    @Column(name = "NAME")
    private String name;

    @Enumerated
    @Column(name = "PAYMENT_METHOD_TYPE")
    private PaymentMethodType paymentMethodType;

    @Column(name = "ENABLED")
    private Boolean enabled;
}
