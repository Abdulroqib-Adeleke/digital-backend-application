package com.groupa.digitalbackendapplication.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
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

    public EmailServiceImpl(@Value("${resend.api.key}") String apiKey,
                            @Value("${resend.sender.email}") String senderEmail) {
        this.senderEmail = senderEmail;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public void sendEmail(EmailDetails emailDetails) {
        Map<String, Object> body = Map.of(
                "from", senderEmail,
                "to", List.of(emailDetails.getRecipient()),
                "subject", emailDetails.getSubject(),
                "html", emailDetails.getMessageBody()
        );

        try {
            restClient.post()
                    .uri("/emails")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Email sent successfully to {}", emailDetails.getRecipient());

        } catch (RestClientResponseException e) {
            log.error("Failed to send email to {}: {} - {}",
                    emailDetails.getRecipient(), e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Email sending failed", e);
        }
    }

    @Override
    public void sendEmail(EmailDetails emailDetails, byte[] file, String fileName) {
        Map<String, Object> body = new HashMap<>();
        body.put("from", senderEmail);
        body.put("to", List.of(emailDetails.getRecipient()));
        body.put("subject", emailDetails.getSubject());
        body.put("html", emailDetails.getMessageBody());

        if (file != null && file.length > 0) {
            String base64Content = Base64.getEncoder().encodeToString(file);
            body.put("attachments", List.of(Map.of(
                    "filename", fileName,
                    "content", base64Content
            )));
        }

        try {
            restClient.post()
                    .uri("/emails")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Email sent successfully to {}", emailDetails.getRecipient());

        } catch (RestClientResponseException e) {
            log.error("Failed to send email to {}: {} - {}",
                    emailDetails.getRecipient(), e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Email sending failed", e);
        }
    }

}
