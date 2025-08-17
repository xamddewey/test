package com.xdw.demobackend.service.auth;

import com.xdw.demobackend.dto.auth.JwtResponse;
import com.xdw.demobackend.dto.auth.LoginRequest;
import com.xdw.demobackend.dto.auth.RegisterRequest;

public interface AuthService {

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求对象
     */
    void register(RegisterRequest registerRequest);

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求对象
     * @return JWT响应对象
     */
    JwtResponse login(LoginRequest loginRequest);

    /**
     * 刷新JWT令牌
     *
     * @param token 旧的JWT令牌
     * @return 新的JWT响应对象
     */
    JwtResponse refreshToken(String token);
}
