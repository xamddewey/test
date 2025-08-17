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
 * User entity class
 * Corresponds to the users table in the database
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("users")
public class User {
    /**
     * Primary key ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * Username, unique
     */
    private String username;

    /**
     * Nickname
     */
    private String nickname;

    /**
     * Avatar URL, optional
     */
    private String avatar;

    /**
     * User bio/introduction, optional
     */
    private String bio;

    /**
     * Phone number, optional
     */
    private String phone;

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
    @Column(onInsertValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    @Column(onUpdateValue = "CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}
