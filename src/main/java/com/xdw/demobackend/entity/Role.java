package com.xdw.demobackend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Role entity class
 * Corresponds to the roles table in the database
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    /**
     * Primary key ID
     */
    private Long id;
    
    /**
     * Role name, unique
     */
    private String roleName;
    
    /**
     * Role description
     */
    private String description;
    
    /**
     * Role type: ADMIN or USER
     */
    private String roleType;
    
    /**
     * Creation timestamp
     */
    private LocalDateTime createdAt;
    
    /**
     * Last update timestamp
     */
    private LocalDateTime updatedAt;
}