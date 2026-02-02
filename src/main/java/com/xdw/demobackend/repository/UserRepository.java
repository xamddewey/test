package com.xdw.demobackend.repository;

import com.xdw.demobackend.entity.User;
import org.babyfish.jimmer.spring.repository.JRepository;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
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
    
    /**
     * Find user by username with eager-loaded roles
     * Uses Jimmer Fetcher API to load associated user roles in a single optimized query
     * Prevents N+1 queries and UnloadedException errors
     * 
     * @param fetcher The Fetcher instance specifying which associations to load
     * @param username The username to search for
     * @return Optional containing the User with loaded roles, or empty if not found
     */
    Optional<User> findByUsername(Fetcher<User> fetcher, String username);
}
