package com.votingapp.voting.repository;

import com.votingapp.voting.entity.OtpVerification;
import com.votingapp.voting.entity.enums.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findFirstByIdentifierAndOtpTypeAndVerifiedFalseOrderByCreatedDateDesc(
            String identifier, OtpType otpType);

    Optional<OtpVerification> findFirstByIdentifierAndOtpTypeOrderByCreatedDateDesc(
            String identifier, OtpType otpType);
}
