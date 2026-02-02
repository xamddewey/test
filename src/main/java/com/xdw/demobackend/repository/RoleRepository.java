package com.xdw.demobackend.repository;

import com.xdw.demobackend.entity.Role;
import org.babyfish.jimmer.spring.repository.JRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Role Repository
 * Jimmer repository for Role entity using Spring Data style
 */
@Repository
public interface RoleRepository extends JRepository<Role, Long> {
    
    /**
     * Find role by role name
     * Spring Data method name convention - Jimmer auto-implements
     */
    Optional<Role> findByRoleName(String roleName);
}
