package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.Constants;
import com.exam.dto.LoginRequest;
import com.exam.dto.LoginResponse;
import com.exam.dto.PasswordChangeDTO;
import com.exam.dto.RegisterRequest;
import com.exam.dto.UserUpdateDTO;
import com.exam.entity.User;
import com.exam.exception.BusinessException;
import com.exam.mapper.UserMapper;
import com.exam.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务
 */
@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);
        
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        
        return new LoginResponse(token, user.getId(), user.getUsername(), 
                user.getNickname(), user.getRole(), user.getAvatar());
    }
    
    /**
     * 用户注册
     */
    @Transactional
    public void register(RegisterRequest request) {
        // 检查用户名是否存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        
        // 检查邮箱是否存在
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            count = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail()));
            if (count > 0) {
                throw new BusinessException("邮箱已被使用");
            }
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(Constants.ROLE_USER);
        user.setStatus(Constants.STATUS_ENABLED);
        
        userMapper.insert(user);
    }
    
    /**
     * 获取当前用户
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "用户未登录");
        }
        
        String username = authentication.getName();
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }
    
    /**
     * 获取用户列表
     */
    public IPage<User> getUserList(int page, int size, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getEmail, keyword);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        
        IPage<User> result = userMapper.selectPage(new Page<>(page, size), wrapper);
        // 清除密码
        result.getRecords().forEach(u -> u.setPassword(null));
        return result;
    }
    
    /**
     * 根据ID获取用户
     */
    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }
    
    /**
     * 更新用户信息
     */
    @Transactional
    public void updateUser(Long id, UserUpdateDTO dto) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 检查邮箱是否被其他用户使用
        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            Long count = userMapper.selectCount(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getEmail, dto.getEmail())
                            .ne(User::getId, id));
            if (count > 0) {
                throw new BusinessException("邮箱已被其他用户使用");
            }
            user.setEmail(dto.getEmail());
        }
        
        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        
        userMapper.updateById(user);
    }
    
    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(Long userId, PasswordChangeDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }
    
    /**
     * 更新用户状态
     */
    @Transactional
    public void updateUserStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 不能禁用管理员自己
        User currentUser = getCurrentUser();
        if (currentUser.getId().equals(id) && status == Constants.STATUS_DISABLED) {
            throw new BusinessException("不能禁用自己的账号");
        }
        
        user.setStatus(status);
        userMapper.updateById(user);
    }
    
    /**
     * 更新用户角色
     */
    @Transactional
    public void updateUserRole(Long id, String role) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 管理员不能修改自己的角色
        User currentUser = getCurrentUser();
        if (currentUser.getId().equals(id)) {
            throw new BusinessException("不能修改自己的角色");
        }
        
        user.setRole(role);
        userMapper.updateById(user);
    }
    
    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 不能删除自己
        User currentUser = getCurrentUser();
        if (currentUser.getId().equals(id)) {
            throw new BusinessException("不能删除自己的账号");
        }
        
        userMapper.deleteById(id);
    }
}
