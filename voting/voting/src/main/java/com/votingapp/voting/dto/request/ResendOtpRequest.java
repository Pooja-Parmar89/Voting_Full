package com.votingapp.voting.dto.request;

import com.votingapp.voting.entity.enums.OtpType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResendOtpRequest {

    @NotBlank
    private String identifier;

    @NotNull
    private OtpType otpType;
}
