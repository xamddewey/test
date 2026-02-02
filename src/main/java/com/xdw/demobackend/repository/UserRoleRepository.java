package com.xdw.demobackend.repository;

import com.xdw.demobackend.entity.UserRole;
import com.xdw.demobackend.entity.UserRoleTable;
import com.xdw.demobackend.entity.UserRoleFetcher;
import com.xdw.demobackend.entity.RoleFetcher;
import com.xdw.demobackend.entity.Role;
import com.xdw.demobackend.entity.RoleTable;
import org.babyfish.jimmer.spring.repository.JRepository;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * UserRole Repository
 * Jimmer repository for UserRole join entity
 */
@Repository
public interface UserRoleRepository extends JRepository<UserRole, Long> {
    
    /**
     * Find all UserRole records by user ID
     * Spring Data method name convention - Jimmer auto-implements
     */
    List<UserRole> findByUserId(long userId);
    
    /**
     * Find all UserRole records by role ID
     * Spring Data method name convention - Jimmer auto-implements
     */
    List<UserRole> findByRoleId(long roleId);
    
    /**
     * Find all UserRole records by user ID with Role association eagerly loaded
     * Uses Jimmer Fetcher API to specify eager loading of the role() association
     * This prevents N+1 queries and "unloaded property" errors during serialization
     * 
     * @param userId the user ID to search for
     * @return List of UserRole entities with role association fully loaded
     */
    default List<UserRole> findByUserIdWithRoles(long userId) {
        UserRoleFetcher fetcher = UserRoleFetcher.$
                .allScalarFields()
                .role(RoleFetcher.$.allScalarFields());
        
        return findByUserId(fetcher, userId);
    }
    
    List<UserRole> findByUserId(Fetcher<UserRole> fetcher, long userId);
}
