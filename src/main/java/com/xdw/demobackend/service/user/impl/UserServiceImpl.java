package com.xdw.demobackend.service.user.impl;

import com.xdw.demobackend.entity.User;
import com.xdw.demobackend.entity.UserDraft;
import com.xdw.demobackend.repository.UserRepository;
import com.xdw.demobackend.repository.UserRoleRepository;
import com.xdw.demobackend.repository.RoleRepository;
import com.xdw.demobackend.security.UserPrincipal;
import com.xdw.demobackend.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务实现类
 * 提供用户管理功能，包括查询、更新和删除操作
 * 使用 Jimmer ORM 进行数据持久化操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    /**
     * 获取所有用户
     *
     * @return 所有用户列表
     */
    @Override
    public List<User> getAllUsers() {
        log.info("获取所有用户");
        return userRepository.findAll();
    }

    /**
     * 根据用户ID获取用户
     *
     * @param id 用户ID
     * @return 用户对象
     * @throws RuntimeException 用户不存在时抛出异常
     */
    @Override
    public User getUserById(Long id) {
        log.info("根据ID获取用户: {}", id);
        return userRepository.findById(id)
            .orElseThrow(() -> {
                log.error("用户不存在，ID: {}", id);
                return new RuntimeException("用户不存在，ID: " + id);
            });
    }

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户对象
     * @throws RuntimeException 用户不存在时抛出异常
     */
    @Override
    public User getUserByUsername(String username) {
        log.info("根据用户名获取用户: {}", username);
        return userRepository.findByUsername(username)
            .orElseThrow(() -> {
                log.error("用户不存在，用户名: {}", username);
                return new RuntimeException("用户不存在，用户名: " + username);
            });
    }

    /**
     * 根据邮箱获取用户
     *
     * @param email 用户邮箱
     * @return 用户对象
     * @throws RuntimeException 用户不存在时抛出异常
     */
    @Override
    public User getUserByEmail(String email) {
        log.info("根据邮箱获取用户: {}", email);
        return userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.error("用户不存在，邮箱: {}", email);
                return new RuntimeException("用户不存在，邮箱: " + email);
            });
    }

    /**
     * 获取当前登录用户
     *
     * @param userPrincipal 当前用户主体信息
     * @return 当前用户对象
     */
    @Override
    public User getCurrentUser(UserPrincipal userPrincipal) {
        log.info("获取当前登录用户: {}", userPrincipal.getUsername());
        return getUserByUsername(userPrincipal.getUsername());
    }

    /**
     * 更新用户信息
     * 使用 Jimmer Draft API 进行不可变对象的更新
     *
     * @param id 用户ID
     * @param user 用户对象（包含要更新的字段）
     * @return 更新后的用户对象
     * @throws RuntimeException 用户不存在时抛出异常
     */
    @Override
    @Transactional
    public User updateUser(Long id, User user) {
        log.info("更新用户信息，ID: {}", id);
        
        // 获取原有用户信息
        User existingUser = getUserById(id);
        
        // 使用 Jimmer Draft API 进行不可变更新
        User updated = UserDraft.$.produce(existingUser, draft -> {
            if (user.username() != null) {
                draft.setUsername(user.username());
            }
            if (user.email() != null) {
                draft.setEmail(user.email());
            }
            if (user.password() != null) {
                draft.setPassword(user.password());
            }
            if (user.nickname() != null) {
                draft.setNickname(user.nickname());
            }
            if (user.avatar() != null) {
                draft.setAvatar(user.avatar());
            }
            if (user.bio() != null) {
                draft.setBio(user.bio());
            }
            if (user.phone() != null) {
                draft.setPhone(user.phone());
            }
            draft.setUpdatedAt(LocalDateTime.now());
        });
        
        User savedUser = userRepository.save(updated);
        log.info("用户信息更新成功，ID: {}", id);
        return savedUser;
    }

    /**
     * 删除用户
     * 同时删除用户的所有角色关联
     *
     * @param id 用户ID
     * @throws RuntimeException 用户不存在时抛出异常
     */
    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("删除用户，ID: {}", id);
        
        // 验证用户是否存在
        User user = getUserById(id);
        
        // 删除用户的所有角色关联
        var userRoles = userRoleRepository.findByUserId(user.id());
        for (var userRole : userRoles) {
            userRoleRepository.deleteById(userRole.id());
        }
        log.debug("用户角色关联删除完成，用户ID: {}", id);
        
        // 删除用户
        userRepository.deleteById(id);
        log.info("用户删除成功，ID: {}", id);
    }
}
