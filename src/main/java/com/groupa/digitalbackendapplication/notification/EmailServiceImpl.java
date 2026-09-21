package com.groupa.digitalbackendapplication.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j

public class EmailServiceImpl implements EmailService {

    private final RestClient restClient;
    private final String senderEmail;


    public EmailServiceImpl(@Value("${brevo.api-key}") String apiKey,  @Value("${brevo.sender-email}") String senderEmail) {
        this.senderEmail = senderEmail;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .defaultHeader("api-key", apiKey)
                .build();
    }

    @Override
    public void sendEmail(EmailDetails emailDetails) {
        Map<String, Object> body = Map.of(
                "sender", Map.of("name", "POI-BANK", "email", senderEmail),
                "to", List.of(Map.of("email", emailDetails.getRecipient())),
                "subject", emailDetails.getSubject(),
                "htmlContent", emailDetails.getMessageBody());
        try {
            restClient.post()
                    .uri("/smtp/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new RuntimeException("Email sending failed", e);
        }


    }

    @Override
    public void sendEmail(EmailDetails emailDetails, byte[] file) {
        Map<String, Object> body = new HashMap<>();
        body.put("sender", Map.of("name", "POI-BANK", "email", senderEmail));
        body.put("to", List.of(Map.of("email", emailDetails.getRecipient())));
        body.put("subject", emailDetails.getSubject());
        body.put("htmlContent", emailDetails.getMessageBody());

        if (file != null && file.length > 0) {
            body.put("attachment", List.of(Map.of(
                    "name", "receipt.pdf",
                    "content", Base64.getEncoder().encodeToString(file))));
        }

        try {
            restClient.post()
                    .uri("/smtp/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            // 4xx/5xx from the provider: the response body says why
            throw new RuntimeException("Email sending failed: " + e.getStatusCode()
                    + " " + e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            throw new RuntimeException("Email sending failed", e);
        }
    }

}
