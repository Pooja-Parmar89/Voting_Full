package com.votingapp.voting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private boolean loginOtpRequired;
    private String token;
    private String tokenType;
    private UserResponse user;
}
