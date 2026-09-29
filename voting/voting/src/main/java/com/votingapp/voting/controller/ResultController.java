package com.votingapp.voting.controller;

import com.votingapp.voting.dto.response.ApiResponse;
import com.votingapp.voting.dto.response.ElectionResultResponse;
import com.votingapp.voting.service.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/elections/{electionId}/results")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;

    @GetMapping
    public ApiResponse<ElectionResultResponse> results(@PathVariable Long electionId) {
        return ApiResponse.success("OK", resultService.getResults(electionId, false));
    }
}
