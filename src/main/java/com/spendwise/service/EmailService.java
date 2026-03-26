package com.spendwise.service;

import com.spendwise.service.interfaces.IEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class EmailService implements IEmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final RestClient restClient;

    @Value("${resend.api-key}")
    private String apiKey;

    @Value("${resend.from:SpendWise <onboarding@resend.dev>}")
    private String fromAddress;

    public EmailService(@Qualifier("resendRestClient") RestClient resendClient) {
        this.restClient = resendClient;
    }

    @Async
    @Override
    public void sendVerificationEmail(String toEmail, String name, String verificationLink) {
        Map<String, Object> body = Map.of(
                "from", fromAddress,
                "to", List.of(toEmail),
                "subject", "SpendWise — Verify your email address",
                "text",
                "Hi " + name + ",\n\n" +
                "Thanks for registering at SpendWise! Please verify your email address by clicking the link below:\n\n" +
                verificationLink + "\n\n" +
                "This link expires in 24 hours.\n\n" +
                "If you did not create an account, you can ignore this email.\n\n" +
                "— The SpendWise Team"
        );

        try {
            restClient.post()
                    .uri("/emails")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            log.debug("Verification email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    @Override
    public void sendPasswordResetEmail(String toEmail, String name, String resetLink) {
        Map<String, Object> body = Map.of(
                "from", fromAddress,
                "to", List.of(toEmail),
                "subject", "SpendWise — Recuperar contraseña",
                "text",
                "Hola " + name + ",\n\n" +
                "Recibimos una solicitud para restablecer la contraseña de tu cuenta SpendWise.\n\n" +
                "Hacé clic en el siguiente enlace para crear una nueva contraseña:\n\n" +
                resetLink + "\n\n" +
                "Este enlace expira en 1 hora.\n\n" +
                "Si no solicitaste restablecer tu contraseña, podés ignorar este email.\n\n" +
                "— El equipo de SpendWise"
        );

        try {
            restClient.post()
                    .uri("/emails")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            log.debug("Password reset email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }
}
