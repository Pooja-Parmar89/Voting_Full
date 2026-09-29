package com.votingapp.voting.entity;

import com.votingapp.voting.entity.enums.OtpType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Generic OTP record. The raw OTP is never stored - only a BCrypt hash of it.
 * A single table + "type" column supports EMAIL_VERIFICATION, MOBILE_VERIFICATION,
 * LOGIN_OTP and PASSWORD_RESET so the same generate/verify pipeline can be reused.
 */
@Entity
@Table(name = "otp_verifications", indexes = {
        @Index(name = "idx_otp_identifier_type", columnList = "identifier,otp_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** email or mobile number the OTP was issued for */
    @Column(nullable = false, length = 150)
    private String identifier;

    @Enumerated(EnumType.STRING)
    @Column(name = "otp_type", nullable = false, length = 30)
    private OtpType otpType;

    @Column(name = "otp_hash", nullable = false)
    private String otpHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts;

    @Column(nullable = false)
    private boolean verified;
}
