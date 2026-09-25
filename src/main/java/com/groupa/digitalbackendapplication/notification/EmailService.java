package com.groupa.digitalbackendapplication.notification;


import com.groupa.digitalbackendapplication.domain.entities.Customer;

public interface EmailService {
    void sendEmail(EmailDetails emailDetails);
    void sendEmail(EmailDetails emailDetails, byte[] file, String fileName);
}
