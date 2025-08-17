package com.xdw.demobackend.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
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
@Table(value = "roles")
public class Role {
    /**
     * Primary key ID
     */
    @Id(keyType = KeyType.Auto)
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
    @Column(onInsertValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    /**
     * Last update timestamp
     */
    @Column(onUpdateValue = "CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}