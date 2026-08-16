package com.spendwise.service.interfaces;

public interface IEmailService {

    void sendVerificationEmail(String toEmail, String name, String verificationLink);
    void sendPasswordResetEmail(String toEmail, String name, String resetLink);

}
