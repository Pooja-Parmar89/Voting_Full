package com.votingapp.voting.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CandidateRequest {

    @NotBlank
    private String name;

    private String description;

    private String party;

    private String symbolUrl;

    private String imageUrl;
}
