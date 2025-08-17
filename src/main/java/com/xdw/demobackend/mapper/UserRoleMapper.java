package com.xdw.demobackend.mapper;

import com.mybatisflex.core.BaseMapper;
import com.xdw.demobackend.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

/**
 * UserRole Mapper interface
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 添加用户角色
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     */
    // 使用 MyBatis Flex 的 insert 方法添加用户角色
    default void addUserRole(Long userId, Long roleId) {
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRole.setCreatedAt(LocalDateTime.now());
        userRole.setUpdatedAt(LocalDateTime.now());
        insert(userRole);
    }
}