package com.votingapp.voting.controller;

import com.votingapp.voting.dto.response.ApiResponse;
import com.votingapp.voting.dto.response.CandidateResponse;
import com.votingapp.voting.service.CandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/elections/{electionId}/candidates")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateService candidateService;

    @GetMapping
    public ApiResponse<List<CandidateResponse>> list(@PathVariable Long electionId) {
        return ApiResponse.success("OK", candidateService.listPublic(electionId));
    }
}
