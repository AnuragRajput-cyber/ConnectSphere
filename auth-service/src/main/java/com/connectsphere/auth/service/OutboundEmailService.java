package com.connectsphere.auth.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class OutboundEmailService {

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String from;

    public OutboundEmailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.mail.enabled:false}") boolean enabled,
            @Value("${app.mail.from:noreply@connectsphere.local}") String from
    ) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.enabled = enabled;
        this.from = from;
    }

    public void sendOtpEmail(String recipientEmail, String otpCode, String subject) {
        if (!enabled) {
            return;
        }
        if (mailSender == null) {
            throw new IllegalStateException("Email delivery is enabled but no mail sender is configured.");
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from);
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(buildOtpBody(otpCode), false);
            mailSender.send(message);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to send verification email. Check SMTP configuration.", ex);
        }
    }

    private String buildOtpBody(String otpCode) {
        return """
                Your ConnectSphere verification code is: %s

                This code will expire soon. If you did not request this, you can ignore this email.
                """.formatted(otpCode);
    }
}
