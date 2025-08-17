package com.xdw.demobackend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;

/**
 * 认证入口点
 * AuthEntryPointJwt 类实现 AuthenticationEntryPoint 接口
 * 用于处理未授权访问的情况
 * 当用户未通过认证时，Spring Security 会调用此类的 commence 方法
 * 该方法会记录日志并返回一个 JSON 响应，包含错误信息和状态码
 */
@Slf4j
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException, ServletException {

        // 记录未授权访问的日志
        log.error("未授权访问: {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        final var body = new HashMap<String, Object>();
        body.put("code", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("message", "认证失败: " + authException.getMessage());
        body.put("path", request.getServletPath());

        final var mapper = new ObjectMapper();
        // 将响应体转换为JSON格式
        mapper.writeValue(response.getOutputStream(), body);
    }
}
