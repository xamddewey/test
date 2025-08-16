package com.xdw.demobackend.dto;

import com.xdw.demobackend.entity.Role;
import com.xdw.demobackend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for User information
 * Contains user details and associated role names
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    
    /**
     * Constructs a UserDTO from a User entity and a list of Role entities
     * 
     * @param user the User entity
     * @param roles the list of Role entities associated with the user
     */
    public UserDTO(User user, List<Role> roles) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.roles = roles.stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());
    }
}