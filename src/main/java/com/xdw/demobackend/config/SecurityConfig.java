package com.xdw.demobackend.config;

import com.xdw.demobackend.security.AuthEntryPointJwt;
import com.xdw.demobackend.security.AuthTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final AuthEntryPointJwt unauthorizedHandler;
    private final AuthTokenFilter authTokenFilter;
    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    /**
     * 密码编码器
     * 使用BCrypt加密算法对密码进行加密存储
     * @return PasswordEncoder 实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器, 用于处理用户认证
     * 通过 AuthenticationConfiguration 获取 AuthenticationManager 实例
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * 配置认证提供者
     * 使用 DaoAuthenticationProvider 处理基于数据库的用户认证
     * DaoAuthenticationProvider 使用 UserDetailsService 加载用户信息
     * 并使用 PasswordEncoder 对密码进行验证
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        // 创建 DaoAuthenticationProvider 实例, 用于处理基于数据库的用户认证
        var authProvider = new DaoAuthenticationProvider();
        // 设置用户详情服务和密码编码器
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * CORS配置源 - 环境感知版本
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 根据配置设置允许的源
        configuration.setAllowedOrigins(
            Arrays.asList(allowedOrigins.split(","))
        );
        // 设置允许的 HTTP 方法
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        // 设置允许的请求头
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        // 设置暴露的响应头
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));
        // 允许携带凭证（如 Cookies）
        configuration.setAllowCredentials(true);
        // 设置预检请求的缓存时间
        configuration.setMaxAge(3600L);
        // 创建 UrlBasedCorsConfigurationSource 实例
        // 用于将 CORS 配置应用到所有请求路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    /**
     * 配置安全过滤链
     * 定义 HTTP 请求的安全策略和过滤器
     * 包括 CORS、CSRF、异常处理、会话管理和请求授权
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 启用 CORS 支持, 使用自定义的 CORS 配置源
            .cors(cors ->
                cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable) // 禁用 CSRF 保护, 因为使用 JWT 令牌进行认证
            // 异常处理, 使用自定义的未授权处理器
            .exceptionHandling(exception ->
                exception.authenticationEntryPoint(unauthorizedHandler))
            // 会话管理, 设置为无状态, 不使用服务器端会话
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 配置请求授权
            .authorizeHttpRequests(authz ->
                authz
                    .requestMatchers("/api/auth/**").permitAll() // 认证相关接口允许所有人访问
                    .requestMatchers("/api/test/**").permitAll() // 测试接口允许所有人访问
                    .requestMatchers("/api/admin/**").hasAnyRole("SUPER_ADMIN", "ADMIN") // 管理员接口需要特定角色
                    .anyRequest().authenticated() // 其他请求需要认证
            );

        http.authenticationProvider(this.authenticationProvider()); // 设置认证提供者
        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class); // 添加自定义的 JWT 过滤器;
        return http.build();
    }
}
