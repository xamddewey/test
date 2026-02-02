package com.xdw.demobackend.repository;

import com.xdw.demobackend.entity.User;
import org.babyfish.jimmer.spring.repository.JRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User Repository
 * Jimmer repository for User entity using Spring Data style
 */
@Repository
public interface UserRepository extends JRepository<User, Long> {
    
    /**
     * Find user by username
     * Spring Data method name convention - Jimmer auto-implements
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email
     * Spring Data method name convention - Jimmer auto-implements
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if username exists
     * Spring Data method name convention - Jimmer auto-implements
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     * Spring Data method name convention - Jimmer auto-implements
     */
    boolean existsByEmail(String email);
}
