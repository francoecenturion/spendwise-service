package com.spendwise.model;

import com.spendwise.model.auth.User;
import com.spendwise.security.AesEncryptor;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "GMAIL_CREDENTIAL")
@Data
public class GmailCredential extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", unique = true)
    private User user;

    @Column(name = "GMAIL_EMAIL")
    private String gmailEmail;

    @Convert(converter = AesEncryptor.class)
    @Column(name = "APP_PASSWORD", columnDefinition = "TEXT")
    private String appPassword;

    @Column(name = "IS_ACTIVE")
    private Boolean isActive;

}
