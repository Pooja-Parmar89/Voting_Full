package com.votingapp.voting.service;

import com.votingapp.voting.dto.request.ChangePasswordRequest;
import com.votingapp.voting.dto.request.UpdateProfileRequest;
import com.votingapp.voting.dto.response.LoginLogResponse;
import com.votingapp.voting.dto.response.UserResponse;
import com.votingapp.voting.entity.User;
import com.votingapp.voting.exception.BadRequestException;
import com.votingapp.voting.repository.LoginLogRepository;
import com.votingapp.voting.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final LoginLogRepository loginLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserResponse getProfile(User user) {
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateProfile(User user, UpdateProfileRequest req) {
        user.setFullName(req.getFullName());
        User saved = userRepository.save(user);
        auditService.log("PROFILE_UPDATED", "USER", "Profile updated: " + user.getEmail(), user.getId(), null);
        return UserResponse.from(saved);
    }

    @Transactional
    public void changePassword(User user, ChangePasswordRequest req) {
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        auditService.log("PASSWORD_CHANGED", "USER", "Password changed: " + user.getEmail(), user.getId(), null);
    }

    public Page<LoginLogResponse> getLoginHistory(User user, Pageable pageable) {
        return loginLogRepository.findByUserOrderByLoginTimeDesc(user, pageable).map(LoginLogResponse::from);
    }
}
