package com.xdw.demobackend.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xdw.demobackend.entity.Role;
import com.xdw.demobackend.entity.User;
import com.xdw.demobackend.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

import static com.xdw.demobackend.entity.table.UserTableDef.USER;

/**
 * User Mapper interface
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查找用户
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    Optional<User> findByUsername(@Param("username") String username);
    
    /**
     * 根据邮箱查找用户
     */
    @Select("SELECT * FROM users WHERE email = #{email}")
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * 根据用户ID查找用户所有角色
     */
    @Select("SELECT r.* " +
            "FROM roles r " +
            "JOIN public.user_roles ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{id}")
    List<Role> findRolesByUserId(@Param("id") Long id);


    /**
     * 检查用户名是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM users WHERE username = #{username}")
    boolean existsByUsername(@Param("username") String username);
    
    /**
     * 检查邮箱是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM users WHERE email = #{email}")
    boolean existsByEmail(@Param("email") String email);
}