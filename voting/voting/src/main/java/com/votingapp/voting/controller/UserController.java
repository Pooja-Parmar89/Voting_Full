package com.votingapp.voting.controller;

import com.votingapp.voting.dto.request.ChangePasswordRequest;
import com.votingapp.voting.dto.request.UpdateProfileRequest;
import com.votingapp.voting.dto.response.ApiResponse;
import com.votingapp.voting.dto.response.LoginLogResponse;
import com.votingapp.voting.dto.response.UserResponse;
import com.votingapp.voting.security.CustomUserPrincipal;
import com.votingapp.voting.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ApiResponse.success("OK", userService.getProfile(principal.getUser()));
    }

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateProfile(@AuthenticationPrincipal CustomUserPrincipal principal,
                                                     @Valid @RequestBody UpdateProfileRequest req) {
        return ApiResponse.success("Profile updated successfully", userService.updateProfile(principal.getUser(), req));
    }

    @PutMapping("/me/password")
    public ApiResponse<Void> changePassword(@AuthenticationPrincipal CustomUserPrincipal principal,
                                             @Valid @RequestBody ChangePasswordRequest req) {
        userService.changePassword(principal.getUser(), req);
        return ApiResponse.success("Password changed successfully");
    }

    @GetMapping("/me/login-history")
    public ApiResponse<Page<LoginLogResponse>> loginHistory(@AuthenticationPrincipal CustomUserPrincipal principal,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success("OK",
                userService.getLoginHistory(principal.getUser(), PageRequest.of(page, size)));
    }
}
