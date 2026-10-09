package com.votingapp.voting.service;

import com.votingapp.voting.entity.enums.OtpType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    /** Returns true if the email was handed to the SMTP server successfully. */
    public boolean sendOtpEmail(String to, OtpType type, String code, long expiryMinutes) {
        String subject = switch (type) {
            case EMAIL_VERIFICATION -> "Verify your email - VoteSecure";
            case LOGIN_OTP -> "Your login OTP - VoteSecure";
            case PASSWORD_RESET -> "Password reset OTP - VoteSecure";
            default -> "Your OTP - VoteSecure";
        };

        String html = """
                <div style="font-family:Arial,sans-serif;max-width:420px;margin:auto;padding:24px;border:1px solid #e5e7eb;border-radius:12px">
                  <h2 style="color:#4f46e5;margin-top:0">VoteSecure</h2>
                  <p>Your one-time password is:</p>
                  <p style="font-size:32px;letter-spacing:6px;font-weight:bold;margin:16px 0">%s</p>
                  <p style="color:#6b7280;font-size:13px">This code expires in %d minutes. If you did not request it, you can ignore this email. Never share this code with anyone.</p>
                </div>
                """.formatted(code, expiryMinutes);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("OTP email sent to {} (type={})", to, type);
            return true;
        } catch (MessagingException | MailException ex) {
            // The OTP code is deliberately NOT logged here.
            log.error("Failed to send OTP email to {} (type={}): {}", to, type, ex.getMessage());
            return false;
        }
    }
}