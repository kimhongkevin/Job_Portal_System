package com.kimhong.job_portal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public boolean sendCVToCompanyHR(String hrEmail,
                                     String hrEmailCC,
                                     String seekerName,
                                     String seekerEmail,
                                     String jobTitle,
                                     String companyName,
                                     String resumeFilePath,
                                     String coverLetter){
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // multipart = true -> required for attachments
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(hrEmail);
            if(hrEmailCC != null && !hrEmailCC.isBlank())
                helper.setCc(hrEmailCC);

            helper.setSubject("New job application - " + jobTitle);

            StringBuilder body = new StringBuilder();
            body.append("Dear HR team at ").append(companyName).append(",\n\n");
            body.append("A new candidate has applied for the position \"").append(jobTitle).append("\".\n\n");
            body.append("Candidate name : ").append(seekerName).append("\n");
            body.append("Candidate email: ").append(seekerEmail).append("\n\n");

            if(coverLetter != null && !coverLetter.isBlank()){
                body.append("Cover letter:\n").append(coverLetter).append("\n\n");
            }

            body.append("The candidate's resume is attached to this email.\n\n");
            body.append("Best regards,\nJob Portal System");
            helper.setText(body.toString());

            // Attach the resume PDF from the stored upload path
            if(resumeFilePath != null && !resumeFilePath.isBlank()){
                Path resumePath = Paths.get(resumeFilePath);
                if(Files.exists(resumePath)){
                    helper.addAttachment(resumePath.getFileName().toString(), resumePath.toFile());
                }
            }

            mailSender.send(message);
            return true;
        } catch (MessagingException | MailException e) {
            System.err.println("Failed to send CV email to " + hrEmail + ": " + e.getMessage());
            return false;
        }
    }

    // Confirmation to the seeker so they know who received their CV
    public void sendCVSentConfirmation(String seekerEmail,String seekerName,String jobTitle,String companyName,String hrEmail){
        sendEmail(seekerEmail,
                "Your CV was sent - "+ jobTitle,
                "Dear "+ seekerName +", your application for \""+ jobTitle
                        +"\" has been sent to "+ companyName +" ("+ hrEmail +") successfully...");
    }

    public void sendPasswordResetEmail(String to, String fullName, String resetLink){
        sendEmail(to,
                "Password reset request - Job Portal",
                "Dear "+ fullName +",\n\n"
                        +"We received a request to reset your password. "
                        +"This link is valid for 15 minutes:\n"
                        + resetLink +"\n\n"
                        +"If you did not request a password reset, you can safely ignore this email.");
    }
}
