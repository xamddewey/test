package com.xdw.demobackend.service.auth.impl;

import com.xdw.demobackend.dto.auth.JwtResponse;
import com.xdw.demobackend.dto.auth.LoginRequest;
import com.xdw.demobackend.dto.auth.RegisterRequest;
import com.xdw.demobackend.entity.Role;
import com.xdw.demobackend.entity.User;
import com.xdw.demobackend.enums.RoleType;
import com.xdw.demobackend.mapper.RoleMapper;
import com.xdw.demobackend.mapper.UserMapper;
import com.xdw.demobackend.mapper.UserRoleMapper;
import com.xdw.demobackend.security.UserPrincipal;
import com.xdw.demobackend.service.auth.AuthService;
import com.xdw.demobackend.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务实现类
 * 提供用户注册、登录和令牌刷新功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final AuthenticationManager authenticationManager;

    /**
     * 用户注册
     * 检查用户名和邮箱是否已存在，若不存在则创建新用户并分配默认角色
     *
     * @param registerRequest 注册请求包含用户名、邮箱和密码
     */
    @Override
    @Transactional
    public void register(RegisterRequest registerRequest) {
        // 检查用户名是否已经存在
        if (userMapper.existsByUsername(registerRequest.getUsername())) {
            log.error("用户名已存在: {}", registerRequest.getUsername());
            throw new IllegalArgumentException("用户名已存在");
        }
        // 检查邮箱是否已经存在
        if (userMapper.existsByEmail(registerRequest.getEmail())) {
            log.error("邮箱已存在: {}", registerRequest.getEmail());
            throw new IllegalArgumentException("邮箱已存在");
        }

        // 创建新用户
        var user = User.builder()
            .username(registerRequest.getUsername())
            .email(registerRequest.getEmail())
            .password(registerRequest.getPassword()) // 密码应加密存储
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        // 保存用户到数据库
        userMapper.insert(user);

        // 分配默认角色
        var role = roleMapper.findByRoleName(RoleType.LEDGER_PARTICIPANT.getName())
            .orElseThrow(() -> new RuntimeException("错误: 默认角色不存在"));

        // 保存用户角色到数据库 
        userRoleMapper.addUserRole(
            user.getId(),
            role.getId()
        );
        log.info("用户注册成功: {}", registerRequest.getUsername());

    }

    /**
     * 用户登录
     * 使用 AuthenticationManager 进行身份验证，生成 JWT 令牌并返回用户信息
     * @param loginRequest 登录请求对象
     * @return JwtResponse 包含 JWT 令牌和用户信息
     */
    @Override
    public JwtResponse login(LoginRequest loginRequest) {
        // 使用 AuthenticationManager 进行身份验证
        var authentication = authenticationManager
            .authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

        // 生成 JWT 令牌
        var jwtToken = JwtUtils.generateJwtToken(authentication);

        // 获取用户详细信息
        var userPrincipal = (UserPrincipal) authentication.getPrincipal();
        var roles = userPrincipal.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .map(authority -> authority.replace("ROLE_", ""))
            .toList();
        log.info("用户登录成功: {}", loginRequest.getUsername());

        return JwtResponse.builder()
            .token(jwtToken)
            .username(userPrincipal.getUsername())
            .email(userPrincipal.getEmail())
            .roles(roles)
            .build();
    }

    /**
     * 刷新JWT令牌
     * 验证旧的JWT令牌，重新生成新的JWT令牌并返回
     * @param token 旧的JWT令牌
     * @return JwtResponse 包含新的JWT令牌和用户信息
     */
    @Override
    public JwtResponse refreshToken(String token) {
        if (JwtUtils.validateJwtToken(token)) {
            // 解析JWT令牌获取用户名
            var username = JwtUtils.getUserNameFromJwtToken(token);
            // 重新生成新的JWT令牌
            var newToken = JwtUtils.generateTokenFromUsername(username);

            // 获取用户详细信息
            var user = userMapper.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

            var roles = roleMapper.findByUserId(user.getId()).stream()
                .map(Role::getRoleName)
                .toList();

            return JwtResponse.builder()
                .token(newToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .build();
        }
        throw new IllegalArgumentException("无效的JWT令牌(token)");
    }
}
