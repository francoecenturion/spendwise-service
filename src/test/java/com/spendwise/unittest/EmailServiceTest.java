package com.spendwise.unittest;

import com.spendwise.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Email Service Unit Tests")
public class EmailServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "apiKey", "re_test_key");
        ReflectionTestUtils.setField(emailService, "fromAddress", "SpendWise <onboarding@resend.dev>");

        doReturn(requestBodyUriSpec).when(restClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), any(String[].class));
        doReturn(requestBodySpec).when(requestBodySpec).body(any(Map.class));
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        doReturn(null).when(responseSpec).toBodilessEntity();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> captureBody() {
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(requestBodySpec).body(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("sendVerificationEmail sends to the correct recipient")
    public void testSendVerificationEmailRecipient() {
        emailService.sendVerificationEmail("john@example.com", "John",
                "http://localhost:8080/auth/verify?token=abc-123");

        List<String> to = (List<String>) captureBody().get("to");
        assertNotNull(to);
        assertEquals("john@example.com", to.get(0));
    }

    @Test
    @DisplayName("sendVerificationEmail sets the expected subject")
    public void testSendVerificationEmailSubject() {
        emailService.sendVerificationEmail("john@example.com", "John",
                "http://localhost:8080/auth/verify?token=abc-123");

        assertEquals("SpendWise — Verify your email address", captureBody().get("subject"));
    }

    @Test
    @DisplayName("sendVerificationEmail body contains the verification link")
    public void testSendVerificationEmailBodyContainsLink() {
        String link = "http://localhost:8080/auth/verify?token=abc-123";

        emailService.sendVerificationEmail("john@example.com", "John", link);

        String text = (String) captureBody().get("text");
        assertNotNull(text);
        assertTrue(text.contains(link), "Email body must contain the verification link");
    }

    @Test
    @DisplayName("sendVerificationEmail body contains the recipient's name")
    public void testSendVerificationEmailBodyContainsName() {
        emailService.sendVerificationEmail("john@example.com", "John",
                "http://localhost:8080/auth/verify?token=abc-123");

        String text = (String) captureBody().get("text");
        assertTrue(text.contains("John"), "Email body must contain the recipient's name");
    }

    @Test
    @DisplayName("sendVerificationEmail calls the Resend API exactly once")
    public void testSendVerificationEmailCalledOnce() {
        emailService.sendVerificationEmail("john@example.com", "John",
                "http://localhost:8080/auth/verify?token=abc-123");

        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(requestBodySpec, Mockito.times(1)).body(captor.capture());
        Mockito.verify(requestBodySpec, Mockito.times(1)).retrieve();
    }
}
