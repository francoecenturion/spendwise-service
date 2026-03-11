package com.spendwise.model;

import com.spendwise.model.auth.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(
    name = "MERCHANT_BINDING",
    uniqueConstraints = @UniqueConstraint(columnNames = {"USER_ID", "MERCHANT_NAME"})
)
@Data
@EqualsAndHashCode(callSuper = true)
public class MerchantBinding extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @Column(name = "MERCHANT_NAME")
    private String merchantName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PAYMENT_METHOD_ID")
    private PaymentMethod paymentMethod;

    @Column(name = "DESCRIPTION")
    private String description;

}
