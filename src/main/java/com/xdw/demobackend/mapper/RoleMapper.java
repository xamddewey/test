package com.xdw.demobackend.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.xdw.demobackend.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

import static com.xdw.demobackend.entity.table.RoleTableDef.ROLE;

/**
 * Role Mapper interface
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据角色名称查找角色
     */
    @Select(
        "SELECT * " +
        "FROM roles " +
        "WHERE role_name = #{roleName}"
    )
    Optional<Role> findByRoleName(@Param("roleName") String roleName);

    /**
     * 根据用户ID查找该用户的所有角色
     */
    @Select(
        "SELECT r.* " +
        "FROM roles r " +
        "INNER JOIN user_roles ur " +
            "ON r.id = ur.role_id " +
        "WHERE ur.user_id = #{userId}"
    )
    List<Role> findByUserId(@Param("userId") Long userId);

    /**
     * 根据角色名称查找角色，使用 MyBatis Flex 的查询方式
     *
     * @param name 角色名称
     * @return 角色实体
     */
    default Role findByNameWithFlex(String name) {
        return selectOneByQuery(QueryWrapper.create()
                .where(ROLE.ROLE_NAME.eq(name)));
    }
}