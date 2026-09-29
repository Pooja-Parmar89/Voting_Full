package com.votingapp.voting.service;

import com.votingapp.voting.entity.LoginLog;
import com.votingapp.voting.entity.User;
import com.votingapp.voting.entity.enums.LoginStatus;
import com.votingapp.voting.repository.LoginLogRepository;
import com.votingapp.voting.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginLogService {

    private final LoginLogRepository loginLogRepository;

    public void recordSuccess(User user, HttpServletRequest request) {
        record(user, user.getEmail(), LoginStatus.SUCCESS, null, request);
    }

    public void recordFailure(String attemptedIdentifier, String reason, HttpServletRequest request) {
        record(null, attemptedIdentifier, LoginStatus.FAILED, reason, request);
    }

    private void record(User user, String identifier, LoginStatus status, String reason, HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String[] browserOs = RequestUtil.parseBrowserAndOs(userAgent);

        LoginLog entry = LoginLog.builder()
                .user(user)
                .attemptedIdentifier(identifier)
                .ipAddress(RequestUtil.getClientIp(request))
                .userAgent(userAgent)
                .browser(browserOs[0])
                .operatingSystem(browserOs[1])
                .loginTime(LocalDateTime.now())
                .status(status)
                .failureReason(reason)
                .build();
        loginLogRepository.save(entry);
    }

    public void recordLogout(User user) {
        loginLogRepository.findFirstByUserAndLogoutTimeIsNullOrderByLoginTimeDesc(user)
                .ifPresent(entry -> {
                    entry.setLogoutTime(LocalDateTime.now());
                    loginLogRepository.save(entry);
                });
    }
}
