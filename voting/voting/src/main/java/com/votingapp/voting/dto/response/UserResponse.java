package com.votingapp.voting.dto.response;

import com.votingapp.voting.entity.User;
import com.votingapp.voting.entity.enums.UserRole;
import com.votingapp.voting.entity.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String mobile;
    private String voterRefId;
    private UserRole role;
    private UserStatus status;
    private boolean emailVerified;
    private boolean mobileVerified;

    public static UserResponse from(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .mobile(u.getMobile())
                .voterRefId(u.getVoterRefId())
                .role(u.getRole())
                .status(u.getStatus())
                .emailVerified(u.isEmailVerified())
                .mobileVerified(u.isMobileVerified())
                .build();
    }
}
