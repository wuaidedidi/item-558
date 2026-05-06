package com.exam.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户更新DTO
 */
@Data
public class UserUpdateDTO {
    
    private String nickname;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$|^$", message = "手机号格式不正确")
    private String phone;
    
    private String avatar;
}
