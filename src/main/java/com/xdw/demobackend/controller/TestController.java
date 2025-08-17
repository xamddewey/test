package com.xdw.demobackend.controller;

import com.xdw.demobackend.dto.common.ApiResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器
 */
@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TestController {
    
    @GetMapping("/public")
    public ApiResult<?> allAccess() {
        return ApiResult.success("公开内容");
    }
    
    @GetMapping("/user")
    @PreAuthorize("hasRole('LEDGER_PARTICIPANT') or hasRole('LEDGER_OWNER') or hasRole('ADMIN')")
    public ApiResult<?> userAccess() {
        return ApiResult.success("用户内容");
    }
    
    @GetMapping("/owner")
    @PreAuthorize("hasRole('LEDGER_OWNER')")
    public ApiResult<?> ownerAccess() {
        return ApiResult.success("账本所有者内容");
    }
    
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<?> adminAccess() {
        return ApiResult.success("管理员内容");
    }
}