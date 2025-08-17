package com.xdw.demobackend.service.user.impl;

import com.xdw.demobackend.entity.User;
import com.xdw.demobackend.mapper.UserMapper;
import com.xdw.demobackend.security.UserPrincipal;
import com.xdw.demobackend.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public List<User> getAllUsers() {
        return userMapper.selectAll();
    }

    @Override
    public User getUserById(Long id) {
        return userMapper.selectOneById(id);
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + username));
    }

    @Override
    public User getUserByEmail(String email) {
        return userMapper.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + email));
    }

    @Override
    public User getCurrentUser(UserPrincipal userPrincipal) {
        return this.getUserById(userPrincipal.getId());
    }

    @Override
    public User updateUser(Long id, User user) {
         Optional
            .ofNullable(this.getUserById(id))
            .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + id));

         user.setId(id);
         userMapper.update(user);
         return user;
    }

    @Override
    public void deleteUser(Long id) {
        Optional
            .ofNullable(this.getUserById(id))
            .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + id));

        userMapper.deleteById(id);
    }
}
