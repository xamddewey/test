package com.xdw.demobackend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User entity class
 * Corresponds to the users table in the database
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    /**
     * Primary key ID
     */
    private Long id;
    
    /**
     * Username, unique
     */
    private String username;
    
    /**
     * Password, stored encrypted
     */
    private String password;
    
    /**
     * Email address, unique
     */
    private String email;
    
    /**
     * Creation timestamp
     */
    private LocalDateTime createdAt;
    
    /**
     * Last update timestamp
     */
    private LocalDateTime updatedAt;
}