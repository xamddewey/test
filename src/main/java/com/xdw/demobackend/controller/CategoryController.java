package com.xdw.demobackend.controller;

import com.xdw.demobackend.dto.common.ApiResult;
import com.xdw.demobackend.dto.expense.CategoryRequest;
import com.xdw.demobackend.dto.expense.CategoryResponse;
import com.xdw.demobackend.security.UserPrincipal;
import com.xdw.demobackend.service.expense.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class CategoryController {
    
    private final CategoryService categoryService;
    
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResult<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            CategoryResponse response = categoryService.createCategory(request, currentUser.getId());
            return ApiResult.success("类别创建成功", response);
        } catch (Exception e) {
            log.error("Failed to create category", e);
            return ApiResult.businessError("创建类别失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/system")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<List<CategoryResponse>> getSystemCategories() {
        try {
            List<CategoryResponse> responses = categoryService.getSystemCategories();
            return ApiResult.success(responses);
        } catch (Exception e) {
            log.error("Failed to get system categories", e);
            return ApiResult.businessError("获取系统类别失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/ledger/{ledgerId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<List<CategoryResponse>> getLedgerCategories(
            @PathVariable Long ledgerId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            List<CategoryResponse> responses = categoryService.getLedgerCategories(
                    ledgerId, currentUser.getId());
            return ApiResult.success(responses);
        } catch (Exception e) {
            log.error("Failed to get ledger categories: {}", ledgerId, e);
            return ApiResult.businessError("获取账本类别失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/ledger/{ledgerId}/add")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<Void> addCategoryToLedger(
            @PathVariable Long ledgerId,
            @RequestParam Long categoryId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            categoryService.addCategoryToLedger(ledgerId, categoryId, currentUser.getId());
            return ApiResult.success("类别添加成功", null);
        } catch (Exception e) {
            log.error("Failed to add category {} to ledger {}", categoryId, ledgerId, e);
            return ApiResult.businessError("添加类别失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/ledger/{ledgerId}/category/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<Void> removeCategoryFromLedger(
            @PathVariable Long ledgerId,
            @PathVariable Long categoryId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            categoryService.removeCategoryFromLedger(ledgerId, categoryId, currentUser.getId());
            return ApiResult.success("类别移除成功", null);
        } catch (Exception e) {
            log.error("Failed to remove category {} from ledger {}", categoryId, ledgerId, e);
            return ApiResult.businessError("移除类别失败: " + e.getMessage());
        }
    }
}
