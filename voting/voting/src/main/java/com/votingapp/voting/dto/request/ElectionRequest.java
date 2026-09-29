package com.votingapp.voting.dto.request;

import com.votingapp.voting.entity.enums.ResultVisibility;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ElectionRequest {

    @NotBlank
    private String title;

    private String description;

    private String electionType;

    @NotNull
    @Future(message = "Start date/time must be in the future")
    private LocalDateTime startDateTime;

    @NotNull
    private LocalDateTime endDateTime;

    private ResultVisibility resultVisibility = ResultVisibility.AFTER_CLOSE;
}
