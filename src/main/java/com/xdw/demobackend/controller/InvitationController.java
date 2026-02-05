package com.xdw.demobackend.controller;

import com.xdw.demobackend.dto.common.ApiResult;
import com.xdw.demobackend.dto.invitation.InviteMemberRequest;
import com.xdw.demobackend.dto.invitation.InvitationResponse;
import com.xdw.demobackend.security.UserPrincipal;
import com.xdw.demobackend.service.invitation.InvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class InvitationController {
    
    private final InvitationService invitationService;
    
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResult<InvitationResponse> sendInvitation(
            @Valid @RequestBody InviteMemberRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            InvitationResponse response = invitationService.sendInvitation(request, currentUser.getId());
            return ApiResult.success("邀请发送成功", response);
        } catch (Exception e) {
            return ApiResult.businessError("发送邀请失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/{invitationId}/accept")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<InvitationResponse> acceptInvitation(
            @PathVariable Long invitationId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            InvitationResponse response = invitationService.acceptInvitation(invitationId, currentUser.getId());
            return ApiResult.success("邀请已接受", response);
        } catch (Exception e) {
            return ApiResult.businessError("接受邀请失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/{invitationId}/reject")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<InvitationResponse> rejectInvitation(
            @PathVariable Long invitationId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            InvitationResponse response = invitationService.rejectInvitation(invitationId, currentUser.getId());
            return ApiResult.success("邀请已拒绝", response);
        } catch (Exception e) {
            return ApiResult.businessError("拒绝邀请失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<List<InvitationResponse>> getMyPendingInvitations(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            List<InvitationResponse> responses = invitationService.getMyPendingInvitations(currentUser.getId());
            return ApiResult.success(responses);
        } catch (Exception e) {
            return ApiResult.businessError("获取邀请列表失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/ledger/{ledgerId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<List<InvitationResponse>> getLedgerInvitations(
            @PathVariable Long ledgerId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            List<InvitationResponse> responses = invitationService.getLedgerInvitations(ledgerId, currentUser.getId());
            return ApiResult.success(responses);
        } catch (Exception e) {
            return ApiResult.businessError("获取账本邀请列表失败: " + e.getMessage());
        }
    }
}
