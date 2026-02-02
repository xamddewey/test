package com.xdw.demobackend.entity;

import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Role entity interface
 * Corresponds to the roles table in the database
 */
@Entity
@Table(name = "roles")
public interface Role {
    /**
     * Primary key ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();
    
    /**
     * Role name, unique
     */
    String roleName();
    
    /**
     * Role description
     */
    @Nullable
    String description();
    
    /**
     * Role type: ADMIN or USER
     */
    String roleType();
    
     /**
      * Creation timestamp
      */
     @Nullable
     LocalDateTime createdAt();
     
     /**
      * Last update timestamp
      */
     @Nullable
     LocalDateTime updatedAt();

    /**
     * Role assignments (inverse side of UserRole.role relationship)
     * Mapped by "role" property on UserRole entity
     */
    @OneToMany(mappedBy = "role")
    List<UserRole> userRoles();
}