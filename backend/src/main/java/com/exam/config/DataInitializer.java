package com.exam.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exam.common.Constants;
import com.exam.entity.User;
import com.exam.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器 - 确保admin密码正确
 */
@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // 检查并修复admin用户密码
        User admin = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, "admin"));
        
        if (admin != null) {
            // 检查密码是否匹配
            if (!passwordEncoder.matches("123456", admin.getPassword())) {
                logger.info("检测到admin密码不正确，正在修复...");
                admin.setPassword(passwordEncoder.encode("123456"));
                userMapper.updateById(admin);
                logger.info("admin密码已修复");
            }
        }
        
        // 检查并修复user用户密码
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, "user"));
        
        if (user != null) {
            if (!passwordEncoder.matches("123456", user.getPassword())) {
                logger.info("检测到user密码不正确，正在修复...");
                user.setPassword(passwordEncoder.encode("123456"));
                userMapper.updateById(user);
                logger.info("user密码已修复");
            }
        }
        
        logger.info("数据初始化检查完成");
    }
}
