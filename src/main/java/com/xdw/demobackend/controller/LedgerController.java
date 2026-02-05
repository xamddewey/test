package com.xdw.demobackend.controller;

import com.xdw.demobackend.dto.common.ApiResult;
import com.xdw.demobackend.dto.ledger.CreateLedgerRequest;
import com.xdw.demobackend.dto.ledger.LedgerResponse;
import com.xdw.demobackend.dto.ledger.MemberResponse;
import com.xdw.demobackend.dto.ledger.UpdateLedgerRequest;
import com.xdw.demobackend.security.UserPrincipal;
import com.xdw.demobackend.service.ledger.LedgerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/ledgers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class LedgerController {
    
    private final LedgerService ledgerService;
    
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResult<LedgerResponse> createLedger(
            @Valid @RequestBody CreateLedgerRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            LedgerResponse response = ledgerService.createLedger(request, currentUser.getId());
            return ApiResult.success("账本创建成功", response);
        } catch (Exception e) {
            return ApiResult.businessError("创建账本失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/{ledgerId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<LedgerResponse> getLedger(
            @PathVariable Long ledgerId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            LedgerResponse response = ledgerService.getLedger(ledgerId, currentUser.getId());
            return ApiResult.success(response);
        } catch (Exception e) {
            return ApiResult.businessError("获取账本失败: " + e.getMessage());
        }
    }
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResult<List<LedgerResponse>> getUserLedgers(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            List<LedgerResponse> responses = ledgerService.getUserLedgers(currentUser.getId());
            return ApiResult.success(responses);
        } catch (Exception e) {
            return ApiResult.businessError("获取账本列表失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/{ledgerId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<LedgerResponse> updateLedger(
            @PathVariable Long ledgerId,
            @Valid @RequestBody UpdateLedgerRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            LedgerResponse response = ledgerService.updateLedger(ledgerId, request, currentUser.getId());
            return ApiResult.success("账本更新成功", response);
        } catch (Exception e) {
            return ApiResult.businessError("更新账本失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{ledgerId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<Void> deleteLedger(
            @PathVariable Long ledgerId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            ledgerService.deleteLedger(ledgerId, currentUser.getId());
            return ApiResult.success("账本删除成功", null);
        } catch (Exception e) {
            return ApiResult.businessError("删除账本失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/{ledgerId}/members")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<List<MemberResponse>> getLedgerMembers(
            @PathVariable Long ledgerId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            List<MemberResponse> responses = ledgerService.getLedgerMembers(ledgerId, currentUser.getId());
            return ApiResult.success(responses);
        } catch (Exception e) {
            return ApiResult.businessError("获取成员列表失败: " + e.getMessage());
        }
    }
}
