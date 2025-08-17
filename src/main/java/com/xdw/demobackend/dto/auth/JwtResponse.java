package com.xdw.demobackend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * JWT认证响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {
    
    private String token;
    private String type = "Bearer";
    private String username;
    private String email;
    private List<String> roles;
}