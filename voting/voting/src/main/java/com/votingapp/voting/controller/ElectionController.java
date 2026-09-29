package com.votingapp.voting.controller;

import com.votingapp.voting.dto.response.ApiResponse;
import com.votingapp.voting.dto.response.ElectionResponse;
import com.votingapp.voting.service.ElectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/elections")
@RequiredArgsConstructor
public class ElectionController {

    private final ElectionService electionService;

    @GetMapping
    public ApiResponse<List<ElectionResponse>> list() {
        return ApiResponse.success("OK", electionService.listPublic());
    }

    @GetMapping("/{id}")
    public ApiResponse<ElectionResponse> get(@PathVariable Long id) {
        return ApiResponse.success("OK", electionService.getPublic(id));
    }
}
