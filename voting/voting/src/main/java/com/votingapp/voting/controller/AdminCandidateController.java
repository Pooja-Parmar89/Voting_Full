package com.votingapp.voting.controller;

import com.votingapp.voting.dto.request.CandidateRequest;
import com.votingapp.voting.dto.response.ApiResponse;
import com.votingapp.voting.dto.response.CandidateResponse;
import com.votingapp.voting.service.CandidateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdminCandidateController {

    private final CandidateService candidateService;

    @GetMapping("/api/admin/elections/{electionId}/candidates")
    public ApiResponse<List<CandidateResponse>> list(@PathVariable Long electionId) {
        return ApiResponse.success("OK", candidateService.listForAdmin(electionId));
    }

    @PostMapping("/api/admin/elections/{electionId}/candidates")
    public ApiResponse<CandidateResponse> create(@PathVariable Long electionId, @Valid @RequestBody CandidateRequest req) {
        return ApiResponse.success("Candidate added", candidateService.create(electionId, req));
    }

    @PutMapping("/api/admin/candidates/{id}")
    public ApiResponse<CandidateResponse> update(@PathVariable Long id, @Valid @RequestBody CandidateRequest req) {
        return ApiResponse.success("Candidate updated", candidateService.update(id, req));
    }

    @DeleteMapping("/api/admin/candidates/{id}")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        candidateService.remove(id);
        return ApiResponse.success("Candidate removed");
    }
}
