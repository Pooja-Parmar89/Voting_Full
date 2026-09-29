package com.votingapp.voting.controller;

import com.votingapp.voting.dto.request.ElectionRequest;
import com.votingapp.voting.dto.response.ApiResponse;
import com.votingapp.voting.dto.response.ElectionResponse;
import com.votingapp.voting.service.ElectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/elections")
@RequiredArgsConstructor
public class AdminElectionController {

    private final ElectionService electionService;

    @GetMapping
    public ApiResponse<List<ElectionResponse>> list() {
        return ApiResponse.success("OK", electionService.listAllForAdmin());
    }

    @GetMapping("/{id}")
    public ApiResponse<ElectionResponse> get(@PathVariable Long id) {
        return ApiResponse.success("OK", electionService.getForAdmin(id));
    }

    @PostMapping
    public ApiResponse<ElectionResponse> create(@Valid @RequestBody ElectionRequest req) {
        return ApiResponse.success("Election created as DRAFT", electionService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<ElectionResponse> update(@PathVariable Long id, @Valid @RequestBody ElectionRequest req) {
        return ApiResponse.success("Election updated", electionService.update(id, req));
    }

    @PutMapping("/{id}/publish")
    public ApiResponse<ElectionResponse> publish(@PathVariable Long id) {
        return ApiResponse.success("Election published and is now UPCOMING", electionService.publish(id));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<ElectionResponse> cancel(@PathVariable Long id) {
        return ApiResponse.success("Election cancelled", electionService.cancel(id));
    }

    @PutMapping("/{id}/declare-results")
    public ApiResponse<ElectionResponse> declareResults(@PathVariable Long id) {
        return ApiResponse.success("Results declared", electionService.declareResults(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        electionService.delete(id);
        return ApiResponse.success("Election deleted");
    }
}
