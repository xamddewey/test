package com.xdw.demobackend.service.user;

import com.xdw.demobackend.entity.User;
import com.xdw.demobackend.security.UserPrincipal;

import java.util.List;

public interface UserService {

    /**
     * 获取所有用户
     */
    List<User> getAllUsers();

    /**
     * 根据ID获取用户
     *
     * @param id 用户ID
     * @return 用户对象
     */
    User getUserById(Long id);

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户对象
     */
    User getUserByUsername(String username);

    /**
     * 根据邮箱获取用户
     *
     * @param email 用户邮箱
     * @return 用户对象
     */
    User getUserByEmail(String email);

    /**
     * 获取当前登录用户
     *
     * @return 当前用户对象
     */
    User getCurrentUser(UserPrincipal userPrincipal);

    /**
     * 更新用户信息
     *
     * @param user 用户对象
     * @return 更新后的用户对象
     */
    User updateUser(Long id, User user);

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    void deleteUser(Long id);
}
