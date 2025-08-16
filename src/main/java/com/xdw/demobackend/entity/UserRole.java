package com.xdw.demobackend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * UserRole entity class
 * Corresponds to the user_roles table in the database
 * Represents the many-to-many relationship between users and roles
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole {
    /**
     * Primary key ID
     */
    private Long id;
    
    /**
     * User ID, references User entity
     */
    private Long userId;
    
    /**
     * Role ID, references Role entity
     */
    private Long roleId;
    
    /**
     * Creation timestamp
     */
    private LocalDateTime createdAt;
    
    /**
     * Last update timestamp
     */
    private LocalDateTime updatedAt;
}