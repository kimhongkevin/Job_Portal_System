package com.kimhong.job_portal.service;

import com.kimhong.job_portal.entity.ApplicationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private void sendEmail(String to,String subject,String body){
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
        } catch (MailException e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }

    public void sendWelcomeEmail(String to,String fullName){
        sendEmail(to,
                "Welcome to Job Portal!",
                "Dear "+ fullName +", welcome to Job Portal...");
    }

    public void sendApplicationConfirmation(String to,String fullName,String jobTitle,String companyName){
        sendEmail(to,
                "Application submitted - "+ jobTitle,
                "Dear "+ fullName +", your application for "+ jobTitle
        +" at "+companyName+" has been submitted successfully...");
    }

    public void sendApplicationStatusUpdate(String to, String fullName,
                                            String jobTitle, ApplicationStatus status) {
        if (status == ApplicationStatus.REVIEWED)
            sendEmail(to, "Application under Review - " + jobTitle,
                    "Dear " + fullName + ", your application is being reviewed...");
        else if (status == ApplicationStatus.ACCEPTED)
            sendEmail(to, "Congratulations! Application Accepted - " + jobTitle,
                    "Dear " + fullName + ", congratulations! You have been accepted...");
        else if (status == ApplicationStatus.REJECTED)
            sendEmail(to, "Application Update - " + jobTitle,
                    "Dear " + fullName + ", thank you for applying, unfortunately...");
    }
}
