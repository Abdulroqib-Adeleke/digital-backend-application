package com.groupa.digitalbackendapplication.notification;

import com.groupa.digitalbackendapplication.domain.entities.Customer;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j

public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public EmailServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendEmail(EmailDetails emailDetails) {
        try{
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(senderEmail);
            mailMessage.setTo(emailDetails.getRecipient());
            mailMessage.setSubject(emailDetails.getSubject());
            mailMessage.setText(emailDetails.getMessageBody());

            javaMailSender.send(mailMessage);
            log.info("Email sent successfully to {}", emailDetails.getRecipient());

        } catch (MailException e) {
            log.error("Failed to send email to {}: {}", emailDetails.getRecipient(), e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }


    }

    @Override
    public void sendEmail(EmailDetails emailDetails, byte[] file, String fileName) {
        Map<String, Object> body = new HashMap<>();
        body.put("sender", Map.of("name", "POI-BANK", "email", senderEmail));
        body.put("to", List.of(Map.of("email", emailDetails.getRecipient())));
        body.put("subject", emailDetails.getSubject());
        body.put("htmlContent", emailDetails.getMessageBody());


        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom(senderEmail);
            helper.setTo(emailDetails.getRecipient());
            helper.setSubject(emailDetails.getSubject());
            helper.setText(emailDetails.getMessageBody());

            if (file != null && file.length > 0) {
                helper.addAttachment(fileName, new ByteArrayResource(file));
            }
            javaMailSender.send(mimeMessage);
            log.info("Email sent successfully to {}", emailDetails.getRecipient());

        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", emailDetails.getRecipient(), e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }

}
