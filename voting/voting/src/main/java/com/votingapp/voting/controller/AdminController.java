package com.votingapp.voting.controller;

import com.votingapp.voting.dto.request.BlockUserRequest;
import com.votingapp.voting.dto.response.*;
import com.votingapp.voting.entity.enums.UserStatus;
import com.votingapp.voting.service.AdminService;
import com.votingapp.voting.service.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final ResultService resultService;

    @GetMapping("/dashboard")
    public ApiResponse<DashboardStatsResponse> dashboard() {
        return ApiResponse.success("OK", adminService.getDashboardStats());
    }

    @GetMapping("/users")
    public ApiResponse<Page<UserResponse>> users(@RequestParam(required = false) UserStatus status,
                                                  @RequestParam(required = false) String search,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success("OK", adminService.searchUsers(status, search, PageRequest.of(page, size)));
    }

    @PutMapping("/users/{id}/block")
    public ApiResponse<Void> block(@PathVariable Long id, @RequestBody(required = false) BlockUserRequest req) {
        adminService.blockUser(id, req != null ? req : new BlockUserRequest());
        return ApiResponse.success("User blocked");
    }

    @PutMapping("/users/{id}/unblock")
    public ApiResponse<Void> unblock(@PathVariable Long id) {
        adminService.unblockUser(id);
        return ApiResponse.success("User unblocked");
    }

    @GetMapping("/login-logs")
    public ApiResponse<Page<LoginLogResponse>> loginLogs(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success("OK", adminService.getLoginLogs(PageRequest.of(page, size)));
    }

    @GetMapping("/audit-logs")
    public ApiResponse<Page<AuditLogResponse>> auditLogs(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success("OK", adminService.getAuditLogs(PageRequest.of(page, size)));
    }

    @GetMapping("/elections/{electionId}/results")
    public ApiResponse<ElectionResultResponse> results(@PathVariable Long electionId) {
        return ApiResponse.success("OK", resultService.getResults(electionId, true));
    }
}
