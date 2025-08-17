package com.xdw.demobackend.controller;

import com.xdw.demobackend.dto.auth.LoginRequest;
import com.xdw.demobackend.dto.auth.RegisterRequest;
import com.xdw.demobackend.dto.common.ApiResult;
import com.xdw.demobackend.enums.ResponseCode;
import com.xdw.demobackend.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录接口
     * 接收用户名和密码，验证后返回JWT令牌
     *
     * @param loginRequest 登录请求对象，包含用户名和密码
     * @return ApiResult 包含JWT令牌或错误信息
     */
    @PostMapping("/login")
    public ApiResult<?> authenticateUser(
        @Valid
        @RequestBody
        LoginRequest loginRequest
    ) {
        try {
            var jwtResponse = authService.login(loginRequest);
            return ApiResult.success("登录成功", jwtResponse);
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage());
            return ApiResult.error(ResponseCode.LOGIN_FAILED, "登录失败: " + e.getMessage());
        }
    }

    /**
     * 用户注册接口
     * 接收注册请求，创建新用户并返回成功消息
     *
     * @param registerRequest 注册请求对象，包含用户名、密码和邮箱
     * @return ApiResult 包含成功消息或错误信息
     */
    @PostMapping("/register")
    public ApiResult<?> registerUser(
        @Valid
        @RequestBody
        RegisterRequest registerRequest
    ) {
        try {
            authService.register(registerRequest);
            return ApiResult.success("用户注册成功");
        } catch (Exception e) {
            log.error("用户注册失败: {}", e.getMessage());
            return ApiResult.error(ResponseCode.BAD_REQUEST, "注册失败: " + e.getMessage());
        }
    }

    /**
     * 刷新JWT令牌接口
     * 接收旧的JWT令牌，返回新的JWT令牌
     *
     * @param token 旧的JWT令牌
     * @return ApiResult 包含新的JWT令牌或错误信息
     */
    @PostMapping("/refresh-token")
    public ApiResult<?> refreshToken(
        @RequestParam("token") String token
    ) {
        try {
            var jwtResponse = authService.refreshToken(token);
            return ApiResult.success("令牌刷新成功", jwtResponse);
        } catch (Exception e) {
            log.error("令牌刷新失败: {}", e.getMessage());
            return ApiResult.error(ResponseCode.BAD_REQUEST, "令牌刷新失败: " + e.getMessage());
        }
    }

}
