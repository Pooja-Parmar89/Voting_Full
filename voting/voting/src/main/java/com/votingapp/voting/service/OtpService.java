package com.votingapp.voting.service;

import com.votingapp.voting.entity.OtpVerification;
import com.votingapp.voting.entity.User;
import com.votingapp.voting.entity.enums.OtpType;
import com.votingapp.voting.exception.BadRequestException;
import com.votingapp.voting.repository.OtpVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpVerificationRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.otp.dev-mode}")
    private boolean devMode;

    @Value("${app.otp.expiry-minutes}")
    private long expiryMinutes;

    @Value("${app.otp.max-attempts}")
    private int maxAttempts;

    @Value("${app.otp.resend-cooldown-seconds}")
    private long resendCooldownSeconds;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Transactional
    public void generateOtp(String identifier, OtpType type, User user) {
        otpRepository.findFirstByIdentifierAndOtpTypeOrderByCreatedDateDesc(identifier, type)
                .ifPresent(last -> {
                    long secondsSince = ChronoUnit.SECONDS.between(last.getCreatedDate(), LocalDateTime.now());
                    if (secondsSince < resendCooldownSeconds) {
                        throw new BadRequestException(
                                "Please wait " + (resendCooldownSeconds - secondsSince) + "s before requesting another OTP");
                    }
                });

        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        LocalDateTime now = LocalDateTime.now();

        OtpVerification otp = OtpVerification.builder()
                .identifier(identifier)
                .otpType(type)
                .otpHash(passwordEncoder.encode(code))
                .user(user)
                .createdDate(now)
                .expiryDate(now.plusMinutes(expiryMinutes))
                .attempts(0)
                .maxAttempts(maxAttempts)
                .verified(false)
                .build();
        otpRepository.save(otp);

        deliver(identifier, type, code);
    }

    /** Email-type OTPs go to email (if enabled); mobile OTPs stay on the console log. */
    private void deliver(String identifier, OtpType type, String code) {
        boolean isEmailChannel = type != OtpType.MOBILE_VERIFICATION;

        if (isEmailChannel && mailEnabled) {
            boolean sent = emailService.sendOtpEmail(identifier, type, code, expiryMinutes);
            if (!sent && devMode) {
                // Email failed - fall back to console so a developer is never locked out.
                log.warn("[DEV-OTP-FALLBACK] type={} identifier={} code={}", type, identifier, code);
            }
            return;
        }

        if (devMode) {
            log.info("[DEV-OTP] type={} identifier={} code={} (expires in {} min)",
                    type, identifier, code, expiryMinutes);
        }
    }

    @Transactional
    public void verifyOtp(String identifier, OtpType type, String code) {
        OtpVerification otp = otpRepository
                .findFirstByIdentifierAndOtpTypeAndVerifiedFalseOrderByCreatedDateDesc(identifier, type)
                .orElseThrow(() -> new BadRequestException("No pending OTP found. Please request a new one."));

        if (LocalDateTime.now().isAfter(otp.getExpiryDate())) {
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }
        if (otp.getAttempts() >= otp.getMaxAttempts()) {
            throw new BadRequestException("Maximum verification attempts exceeded. Please request a new OTP.");
        }

        otp.setAttempts(otp.getAttempts() + 1);

        if (!passwordEncoder.matches(code, otp.getOtpHash())) {
            otpRepository.save(otp);
            throw new BadRequestException("Invalid OTP code");
        }

        otp.setVerified(true);
        otpRepository.save(otp);
    }
}