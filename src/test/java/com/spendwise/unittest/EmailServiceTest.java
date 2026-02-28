package com.spendwise.unittest;

import com.spendwise.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Email Service Unit Tests")
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    @DisplayName("sendVerificationEmail calls mailSender.send with the correct recipient")
    public void testSendVerificationEmailRecipient() {

        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.sendVerificationEmail(
                "john@example.com",
                "John",
                "http://localhost:8080/auth/verify?token=abc-123"
        );

        // Assert
        Mockito.verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sent = messageCaptor.getValue();
        assertNotNull(sent.getTo());
        assertEquals("john@example.com", sent.getTo()[0]);
    }

    @Test
    @DisplayName("sendVerificationEmail sets the expected subject")
    public void testSendVerificationEmailSubject() {

        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.sendVerificationEmail("john@example.com", "John",
                "http://localhost:8080/auth/verify?token=abc-123");

        // Assert
        Mockito.verify(mailSender).send(messageCaptor.capture());
        assertEquals("SpendWise — Verify your email address", messageCaptor.getValue().getSubject());
    }

    @Test
    @DisplayName("sendVerificationEmail body contains the verification link")
    public void testSendVerificationEmailBodyContainsLink() {

        // Arrange
        String link = "http://localhost:8080/auth/verify?token=abc-123";
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.sendVerificationEmail("john@example.com", "John", link);

        // Assert
        Mockito.verify(mailSender).send(messageCaptor.capture());
        String body = messageCaptor.getValue().getText();
        assertNotNull(body);
        assertTrue(body.contains(link), "Email body must contain the verification link");
    }

    @Test
    @DisplayName("sendVerificationEmail body contains the recipient's name")
    public void testSendVerificationEmailBodyContainsName() {

        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.sendVerificationEmail("john@example.com", "John",
                "http://localhost:8080/auth/verify?token=abc-123");

        // Assert
        Mockito.verify(mailSender).send(messageCaptor.capture());
        String body = messageCaptor.getValue().getText();
        assertTrue(body.contains("John"), "Email body must contain the recipient's name");
    }

    @Test
    @DisplayName("sendVerificationEmail calls mailSender.send exactly once")
    public void testSendVerificationEmailCalledOnce() {

        // Act
        emailService.sendVerificationEmail("john@example.com", "John",
                "http://localhost:8080/auth/verify?token=abc-123");

        // Assert
        Mockito.verify(mailSender, Mockito.times(1)).send(Mockito.any(SimpleMailMessage.class));
        Mockito.verifyNoMoreInteractions(mailSender);
    }
}
