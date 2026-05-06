package com.exam.dto;

import lombok.Data;

/**
 * 登录响应DTO
 */
@Data
public class LoginResponse {
    
    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String username;
    private String nickname;
    private String role;
    private String avatar;
    
    public LoginResponse(String token, Long userId, String username, String nickname, String role, String avatar) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.role = role;
        this.avatar = avatar;
    }
}
