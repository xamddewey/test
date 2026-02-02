package com.xdw.demobackend.entity;

import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User entity interface
 * Corresponds to the users table in the database
 * 
 * Jimmer entities are immutable interfaces with getter methods.
 * Jimmer generates implementations via jimmer-apt annotation processor.
 */
@Entity
@Table(name = "users")
public interface User {
    /**
     * Primary key ID - auto-generated
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();

    /**
     * Username, unique
     */
    String username();

    /**
     * Nickname
     */
    String nickname();

    /**
     * Avatar URL, optional
     */
    @Nullable
    String avatar();

    /**
     * User bio/introduction, optional
     */
    @Nullable
    String bio();

    /**
     * Phone number, optional
     */
    @Nullable
    String phone();

    /**
     * Password, stored encrypted
     */
    String password();

    /**
     * Email address, unique
     */
    String email();

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
     * User role assignments (inverse side of UserRole.user relationship)
     * Mapped by "user" property on UserRole entity
     */
    @OneToMany(mappedBy = "user")
    List<UserRole> userRoles();
}
