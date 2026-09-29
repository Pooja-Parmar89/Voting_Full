package com.votingapp.voting.controller;

import com.votingapp.voting.dto.request.VoteRequest;
import com.votingapp.voting.dto.response.ApiResponse;
import com.votingapp.voting.dto.response.VoteResponse;
import com.votingapp.voting.dto.response.VotingStatusResponse;
import com.votingapp.voting.security.CustomUserPrincipal;
import com.votingapp.voting.service.VoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/elections/{electionId}")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    @PostMapping("/votes")
    public ApiResponse<VoteResponse> vote(@AuthenticationPrincipal CustomUserPrincipal principal,
                                           @PathVariable Long electionId,
                                           @Valid @RequestBody VoteRequest req) {
        VoteResponse response = voteService.castVote(principal.getUser(), electionId, req);
        return ApiResponse.success("Vote submitted successfully", response);
    }

    @GetMapping("/voting-status")
    public ApiResponse<VotingStatusResponse> status(@AuthenticationPrincipal CustomUserPrincipal principal,
                                                      @PathVariable Long electionId) {
        return ApiResponse.success("OK", voteService.votingStatus(principal.getUser(), electionId));
    }
}
