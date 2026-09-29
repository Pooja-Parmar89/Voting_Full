package com.votingapp.voting.controller;

import com.votingapp.voting.dto.request.*;
import com.votingapp.voting.dto.response.ApiResponse;
import com.votingapp.voting.dto.response.AuthResponse;
import com.votingapp.voting.entity.User;
import com.votingapp.voting.security.CustomUserPrincipal;
import com.votingapp.voting.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return ApiResponse.success("Registration successful. Please verify the OTP sent to your email and mobile number.");
    }

    @PostMapping("/verify-email-otp")
    public ApiResponse<Void> verifyEmailOtp(@Valid @RequestBody VerifyOtpRequest req) {
        req.setOtpType(com.votingapp.voting.entity.enums.OtpType.EMAIL_VERIFICATION);
        authService.verifyOtp(req);
        return ApiResponse.success("Email verified successfully");
    }

    @PostMapping("/verify-mobile-otp")
    public ApiResponse<Void> verifyMobileOtp(@Valid @RequestBody VerifyOtpRequest req) {
        req.setOtpType(com.votingapp.voting.entity.enums.OtpType.MOBILE_VERIFICATION);
        authService.verifyOtp(req);
        return ApiResponse.success("Mobile number verified successfully");
    }

    @PostMapping("/verify-otp")
    public ApiResponse<Void> verifyOtp(@Valid @RequestBody VerifyOtpRequest req) {
        authService.verifyOtp(req);
        return ApiResponse.success("OTP verified successfully");
    }

    @PostMapping("/resend-otp")
    public ApiResponse<Void> resendOtp(@Valid @RequestBody ResendOtpRequest req) {
        authService.resendOtp(req);
        return ApiResponse.success("OTP resent successfully");
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req, HttpServletRequest httpRequest) {
        AuthResponse response = authService.login(req, httpRequest);
        String message = response.isLoginOtpRequired()
                ? "OTP sent to your registered email. Please verify to complete login."
                : "Login successful";
        return ApiResponse.success(message, response);
    }

    @PostMapping("/verify-login-otp")
    public ApiResponse<AuthResponse> verifyLoginOtp(@Valid @RequestBody VerifyOtpRequest req, HttpServletRequest httpRequest) {
        AuthResponse response = authService.verifyLoginOtp(req, httpRequest);
        return ApiResponse.success("Login successful", response);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        authService.forgotPassword(req);
        return ApiResponse.success("If an account exists, a password reset OTP has been sent");
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req);
        return ApiResponse.success("Password reset successfully. Please log in with your new password.");
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal CustomUserPrincipal principal) {
        authService.logout(principal.getUser());
        return ApiResponse.success("Logged out successfully");
    }
}
