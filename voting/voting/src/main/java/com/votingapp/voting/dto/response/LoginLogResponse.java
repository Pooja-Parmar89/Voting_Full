package com.votingapp.voting.dto.response;

import com.votingapp.voting.entity.LoginLog;
import com.votingapp.voting.entity.enums.LoginStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginLogResponse {
    private Long id;
    private String userFullName;
    private String attemptedIdentifier;
    private String ipAddress;
    private String userAgent;
    private String browser;
    private String operatingSystem;
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
    private LoginStatus status;
    private String failureReason;

    public static LoginLogResponse from(LoginLog l) {
        return LoginLogResponse.builder()
                .id(l.getId())
                .userFullName(l.getUser() != null ? l.getUser().getFullName() : null)
                .attemptedIdentifier(l.getAttemptedIdentifier())
                .ipAddress(l.getIpAddress())
                .userAgent(l.getUserAgent())
                .browser(l.getBrowser())
                .operatingSystem(l.getOperatingSystem())
                .loginTime(l.getLoginTime())
                .logoutTime(l.getLogoutTime())
                .status(l.getStatus())
                .failureReason(l.getFailureReason())
                .build();
    }
}
