package com.xdw.demobackend.entity;

import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;
import java.time.LocalDateTime;

/**
 * UserRole entity interface
 * Corresponds to the user_roles table in the database
 * Represents the many-to-many relationship between users and roles
 * 
 * This is an explicit join entity (not @ManyToMany) to preserve audit timestamps
 * (createdAt, updatedAt).
 */
@Entity
@Table(name = "user_roles")
public interface UserRole {
    /**
     * Primary key ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();
    
    /**
     * Associated User entity (owning side of the relationship)
     * Foreign key references the users table
     */
    @ManyToOne
    User user();
    
    /**
     * Associated Role entity (owning side of the relationship)
     * Foreign key references the roles table
     * Eagerly loaded to prevent "unloaded property" errors
     */
    @ManyToOne
    Role role();
    
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
}