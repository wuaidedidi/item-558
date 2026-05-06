package com.exam;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 练习题与考试系统启动类
 */
@SpringBootApplication
@MapperScan("com.exam.mapper")
public class ExamApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ExamApplication.class, args);
    }
}
