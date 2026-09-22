package com.groupa.digitalbackendapplication.notification;


public interface EmailService {
    void sendEmail(EmailDetails emailDetails);
    void sendEmail(EmailDetails emailDetails, byte[] file);
}
