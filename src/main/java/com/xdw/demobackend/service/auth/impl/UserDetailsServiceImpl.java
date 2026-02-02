package com.xdw.demobackend.service.auth.impl;

import com.xdw.demobackend.entity.User;
import com.xdw.demobackend.repository.UserRepository;
import com.xdw.demobackend.repository.UserRoleRepository;
import com.xdw.demobackend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * UserDetailsServiceImpl 实现 UserDetailsService 接口
 * 用于加载用户详细信息
 * 通过用户名查找用户并返回 UserDetails 对象
 * 如果用户不存在，则抛出 UsernameNotFoundException 异常
 * 
 * 使用 Jimmer Repository 实现，支持高效的关联数据加载
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    /**
     * 根据用户名加载用户详细信息
     *
     * @param username 用户名
     * @return UserDetails 用户详细信息
     * @throws UsernameNotFoundException 如果用户不存在，则抛出异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
        
        List<String> roleNames = userRoleRepository.findByUserIdWithRoles(user.id())
                .stream()
                .map(userRole -> userRole.role().roleName())
                .toList();

        return UserPrincipal.create(user, roleNames);
    }
}
