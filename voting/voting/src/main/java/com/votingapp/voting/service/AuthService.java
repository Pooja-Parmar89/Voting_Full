package com.votingapp.voting.service;

import com.votingapp.voting.dto.request.*;
import com.votingapp.voting.dto.response.AuthResponse;
import com.votingapp.voting.dto.response.UserResponse;
import com.votingapp.voting.entity.User;
import com.votingapp.voting.entity.enums.OtpType;
import com.votingapp.voting.entity.enums.UserRole;
import com.votingapp.voting.entity.enums.UserStatus;
import com.votingapp.voting.exception.BadRequestException;
import com.votingapp.voting.exception.ConflictException;
import com.votingapp.voting.exception.UnauthorizedException;
import com.votingapp.voting.repository.UserRepository;
import com.votingapp.voting.security.JwtUtil;
import com.votingapp.voting.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditService auditService;
    private final LoginLogService loginLogService;

    @Value("${app.otp.login-required}")
    private boolean loginOtpRequired;

    @Transactional
    public void register(RegisterRequest req) {
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new BadRequestException("Password and confirm password do not match");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ConflictException("An account with this email already exists");
        }
        if (userRepository.existsByMobile(req.getMobile())) {
            throw new ConflictException("An account with this mobile number already exists");
        }

        User user = User.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .mobile(req.getMobile())
                .password(passwordEncoder.encode(req.getPassword()))
                .voterRefId(req.getVoterRefId())
                .role(UserRole.VOTER)
                .status(UserStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .mobileVerified(false)
                .build();
        user = userRepository.save(user);

        otpService.generateOtp(user.getEmail(), OtpType.EMAIL_VERIFICATION, user);
        otpService.generateOtp(user.getMobile(), OtpType.MOBILE_VERIFICATION, user);

        auditService.log("USER_REGISTERED", "AUTH", "New voter registration: " + user.getEmail(), user.getId(), null);
    }

    @Transactional
    public void verifyOtp(VerifyOtpRequest req) {
        otpService.verifyOtp(req.getIdentifier(), req.getOtpType(), req.getCode());

        if (req.getOtpType() == OtpType.EMAIL_VERIFICATION || req.getOtpType() == OtpType.MOBILE_VERIFICATION) {
            User user = userRepository.findByEmailOrMobile(req.getIdentifier())
                    .orElseThrow(() -> new BadRequestException("User not found"));

            if (req.getOtpType() == OtpType.EMAIL_VERIFICATION) {
                user.setEmailVerified(true);
                auditService.log("EMAIL_VERIFIED", "AUTH", "Email verified: " + user.getEmail(), user.getId(), null);
            } else {
                user.setMobileVerified(true);
                auditService.log("MOBILE_VERIFIED", "AUTH", "Mobile verified: " + user.getMobile(), user.getId(), null);
            }

            if (user.isFullyVerified() && user.getStatus() == UserStatus.PENDING_VERIFICATION) {
                user.setStatus(UserStatus.ACTIVE);
            }
            userRepository.save(user);
        }
    }

    @Transactional
    public void resendOtp(ResendOtpRequest req) {
        User user = userRepository.findByEmailOrMobile(req.getIdentifier()).orElse(null);
        otpService.generateOtp(req.getIdentifier(), req.getOtpType(), user);
    }

    @Transactional
    public AuthResponse login(LoginRequest req, HttpServletRequest httpRequest) {
        User user = userRepository.findByEmailOrMobile(req.getIdentifier()).orElse(null);

        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            loginLogService.recordFailure(req.getIdentifier(), "Invalid credentials", httpRequest);
            auditService.log("LOGIN_FAILED", "AUTH", "Failed login for: " + req.getIdentifier(), null,
                    RequestUtil.getClientIp(httpRequest));
            throw new UnauthorizedException("Invalid email/mobile or password");
        }

        if (user.getStatus() == UserStatus.BLOCKED) {
            loginLogService.recordFailure(req.getIdentifier(), "Account blocked", httpRequest);
            auditService.log("LOGIN_FAILED", "AUTH", "Blocked account attempted login: " + user.getEmail(),
                    user.getId(), RequestUtil.getClientIp(httpRequest));
            throw new UnauthorizedException("Your account has been blocked. Please contact support.");
        }

        if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            loginLogService.recordFailure(req.getIdentifier(), "Account not verified", httpRequest);
            throw new UnauthorizedException("Please verify your email and mobile number before logging in");
        }

        if (loginOtpRequired) {
            otpService.generateOtp(user.getEmail(), OtpType.LOGIN_OTP, user);
            return AuthResponse.builder().loginOtpRequired(true).build();
        }

        return completeLogin(user, httpRequest);
    }

    @Transactional
    public AuthResponse verifyLoginOtp(VerifyOtpRequest req, HttpServletRequest httpRequest) {
        otpService.verifyOtp(req.getIdentifier(), OtpType.LOGIN_OTP, req.getCode());
        User user = userRepository.findByEmailOrMobile(req.getIdentifier())
                .orElseThrow(() -> new BadRequestException("User not found"));
        return completeLogin(user, httpRequest);
    }

    private AuthResponse completeLogin(User user, HttpServletRequest httpRequest) {
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        loginLogService.recordSuccess(user, httpRequest);
        auditService.log("LOGIN_SUCCESS", "AUTH", "User logged in: " + user.getEmail(), user.getId(),
                RequestUtil.getClientIp(httpRequest));

        return AuthResponse.builder()
                .loginOtpRequired(false)
                .token(token)
                .tokenType("Bearer")
                .user(UserResponse.from(user))
                .build();
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest req) {
        // Deliberately silent on unknown identifiers so the endpoint can't be used
        // to probe which emails/mobiles are registered.
        userRepository.findByEmailOrMobile(req.getIdentifier())
                .ifPresent(user -> otpService.generateOtp(user.getEmail(), OtpType.PASSWORD_RESET, user));
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new BadRequestException("Password and confirm password do not match");
        }
        User user = userRepository.findByEmailOrMobile(req.getIdentifier())
                .orElseThrow(() -> new BadRequestException("User not found"));

        otpService.verifyOtp(user.getEmail(), OtpType.PASSWORD_RESET, req.getCode());

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        auditService.log("PASSWORD_RESET", "AUTH", "Password reset for: " + user.getEmail(), user.getId(), null);
    }

    @Transactional
    public void logout(User user) {
        loginLogService.recordLogout(user);
        auditService.log("LOGOUT", "AUTH", "User logged out: " + user.getEmail(), user.getId(), null);
    }
}
