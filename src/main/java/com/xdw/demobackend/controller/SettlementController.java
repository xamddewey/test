package com.xdw.demobackend.controller;

import com.xdw.demobackend.dto.common.ApiResult;
import com.xdw.demobackend.dto.settlement.CreateSettlementRequest;
import com.xdw.demobackend.dto.settlement.SettlementCalculationResponse;
import com.xdw.demobackend.dto.settlement.SettlementResponse;
import com.xdw.demobackend.security.UserPrincipal;
import com.xdw.demobackend.service.settlement.SettlementService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settlements")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SettlementController {

    private static final Logger log = LoggerFactory.getLogger(SettlementController.class);

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @GetMapping("/calculate/{ledgerId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<SettlementCalculationResponse> calculateSettlement(
            @PathVariable Long ledgerId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            SettlementCalculationResponse response = settlementService.calculateSettlement(ledgerId, currentUser.getId());
            return ApiResult.success(response);
        } catch (Exception e) {
            log.error("Failed to calculate settlement: {}", ledgerId, e);
            return ApiResult.businessError("计算结算失败: " + e.getMessage());
        }
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResult<SettlementResponse> createSettlement(
            @Valid @RequestBody CreateSettlementRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            SettlementResponse response = settlementService.createSettlement(request, currentUser.getId());
            return ApiResult.success("结算创建成功", response);
        } catch (Exception e) {
            log.error("Failed to create settlement", e);
            return ApiResult.businessError("创建结算失败: " + e.getMessage());
        }
    }

    @PostMapping("/{settlementId}/complete")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<SettlementResponse> completeSettlement(
            @PathVariable Long settlementId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            SettlementResponse response = settlementService.completeSettlement(settlementId, currentUser.getId());
            return ApiResult.success("结算已完成", response);
        } catch (Exception e) {
            log.error("Failed to complete settlement: {}", settlementId, e);
            return ApiResult.businessError("完成结算失败: " + e.getMessage());
        }
    }

    @GetMapping("/ledger/{ledgerId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<List<SettlementResponse>> getLedgerSettlements(
            @PathVariable Long ledgerId,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            List<SettlementResponse> responses = settlementService.getLedgerSettlements(ledgerId, status, currentUser.getId());
            return ApiResult.success(responses);
        } catch (Exception e) {
            log.error("Failed to get ledger settlements: {}", ledgerId, e);
            return ApiResult.businessError("获取账本结算列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<List<SettlementResponse>> getMySettlements(
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            List<SettlementResponse> responses = settlementService.getMySettlements(currentUser.getId(), status);
            return ApiResult.success(responses);
        } catch (Exception e) {
            log.error("Failed to get my settlements", e);
            return ApiResult.businessError("获取我的结算列表失败: " + e.getMessage());
        }
    }
}
