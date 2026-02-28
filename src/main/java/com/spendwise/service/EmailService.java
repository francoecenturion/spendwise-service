package com.spendwise.service;

import com.spendwise.service.interfaces.IEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService implements IEmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendVerificationEmail(String toEmail, String name, String verificationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("SpendWise — Verify your email address");
        message.setText(
                "Hi " + name + ",\n\n" +
                "Thanks for registering at SpendWise! Please verify your email address by clicking the link below:\n\n" +
                verificationLink + "\n\n" +
                "This link expires in 24 hours.\n\n" +
                "If you did not create an account, you can ignore this email.\n\n" +
                "— The SpendWise Team"
        );

        mailSender.send(message);
        log.debug("Verification email sent to {}", toEmail);
    }
}
