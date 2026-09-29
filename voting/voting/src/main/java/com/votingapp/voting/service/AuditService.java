package com.votingapp.voting.service;

import com.votingapp.voting.entity.AuditLog;
import com.votingapp.voting.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(String action, String module, String description, Long userId, String ipAddress) {
        AuditLog entry = AuditLog.builder()
                .userId(userId)
                .action(action)
                .module(module)
                .description(description)
                .ipAddress(ipAddress)
                .createdDate(LocalDateTime.now())
                .createdBy(currentActor())
                .build();
        auditLogRepository.save(entry);
    }

    public void log(String action, String module, String description) {
        log(action, module, description, null, null);
    }

    private String currentActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "SYSTEM";
        }
        return auth.getName();
    }
}
