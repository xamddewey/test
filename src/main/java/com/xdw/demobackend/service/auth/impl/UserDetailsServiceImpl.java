package com.xdw.demobackend.service.auth.impl;

import com.xdw.demobackend.entity.Role;
import com.xdw.demobackend.entity.User;
import com.xdw.demobackend.mapper.RoleMapper;
import com.xdw.demobackend.mapper.UserMapper;
import com.xdw.demobackend.mapper.UserRoleMapper;
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
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;

    /**
     * 根据用户名加载用户详细信息
     *
     * @param username 用户名
     * @return UserDetails 用户详细信息
     * @throws UsernameNotFoundException 如果用户不存在，则抛出异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
        List<Role> roles = roleMapper.findByUserId(user.getId());
        List<String> roleNames = roles.stream()
                .map(Role::getRoleName)
                .toList();

        return UserPrincipal.create(user, roleNames);
    }
}
