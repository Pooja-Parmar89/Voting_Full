package com.votingapp.voting.dto.response;

import com.votingapp.voting.entity.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private Long userId;
    private String action;
    private String module;
    private String description;
    private String ipAddress;
    private LocalDateTime createdDate;
    private String createdBy;

    public static AuditLogResponse from(AuditLog a) {
        return AuditLogResponse.builder()
                .id(a.getId())
                .userId(a.getUserId())
                .action(a.getAction())
                .module(a.getModule())
                .description(a.getDescription())
                .ipAddress(a.getIpAddress())
                .createdDate(a.getCreatedDate())
                .createdBy(a.getCreatedBy())
                .build();
    }
}
