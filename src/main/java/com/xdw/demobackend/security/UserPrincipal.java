package com.xdw.demobackend.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xdw.demobackend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * UserPrincipal class implements UserDetails interface
 * Represents the authenticated user in the security context
 */
    @Data
public class UserPrincipal implements UserDetails {
    private Long id;
    private String username;
    private String email;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    /**
     * 创建UserPrincipal实例
     *
     * @param user  用户实体
     * @param roles 用户角色列表
     * @return UserPrincipal实例
     */
     public static UserPrincipal create(User user, List<String> roles) {
         var authorities = roles.stream()
             .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
             .toList();
         return new UserPrincipal(
             user.id(),
             user.username(),
             user.email(),
             user.password(),
             authorities
         );
     }

    public UserPrincipal(
            Long id,
            String username,
            String email,
            String password,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    /**
     * 获取用户权限列表
     *
     * @return 用户权限集合
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id) &&
            Objects.equals(username, that.username) &&
            Objects.equals(email, that.email);
    }
}
