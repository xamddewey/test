package com.xdw.demobackend.controller;

import com.xdw.demobackend.dto.common.ApiResult;
import com.xdw.demobackend.dto.expense.CreateExpenseRequest;
import com.xdw.demobackend.dto.expense.ExpenseResponse;
import com.xdw.demobackend.dto.expense.UpdateExpenseRequest;
import com.xdw.demobackend.security.UserPrincipal;
import com.xdw.demobackend.service.expense.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ExpenseController {
    
    private final ExpenseService expenseService;
    
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResult<ExpenseResponse> createExpense(
            @Valid @RequestBody CreateExpenseRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            ExpenseResponse response = expenseService.createExpense(request, currentUser.getId());
            return ApiResult.success("记账条目创建成功", response);
        } catch (Exception e) {
            log.error("Failed to create expense", e);
            return ApiResult.businessError("创建记账条目失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/{expenseId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<ExpenseResponse> getExpense(
            @PathVariable Long expenseId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            ExpenseResponse response = expenseService.getExpenseById(expenseId, currentUser.getId());
            return ApiResult.success(response);
        } catch (Exception e) {
            log.error("Failed to get expense: {}", expenseId, e);
            return ApiResult.businessError("获取记账条目失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/ledger/{ledgerId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<List<ExpenseResponse>> getLedgerExpenses(
            @PathVariable Long ledgerId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        try {
            if (page != null && size != null) {
                Pageable pageable = PageRequest.of(page, size);
                Page<ExpenseResponse> responsePage = expenseService.getLedgerExpensesPaged(
                        ledgerId, currentUser.getId(), pageable);
                return ApiResult.success(responsePage.getContent());
            } else {
                List<ExpenseResponse> responses = expenseService.getLedgerExpenses(
                        ledgerId, currentUser.getId());
                return ApiResult.success(responses);
            }
        } catch (Exception e) {
            log.error("Failed to get ledger expenses: {}", ledgerId, e);
            return ApiResult.businessError("获取账本记账列表失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/{expenseId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<ExpenseResponse> updateExpense(
            @PathVariable Long expenseId,
            @Valid @RequestBody UpdateExpenseRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            ExpenseResponse response = expenseService.updateExpense(
                    expenseId, request, currentUser.getId());
            return ApiResult.success("记账条目更新成功", response);
        } catch (Exception e) {
            log.error("Failed to update expense: {}", expenseId, e);
            return ApiResult.businessError("更新记账条目失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{expenseId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<Void> deleteExpense(
            @PathVariable Long expenseId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            expenseService.deleteExpense(expenseId, currentUser.getId());
            return ApiResult.success("记账条目删除成功", null);
        } catch (Exception e) {
            log.error("Failed to delete expense: {}", expenseId, e);
            return ApiResult.businessError("删除记账条目失败: " + e.getMessage());
        }
    }
}
