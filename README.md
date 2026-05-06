# 练习题与考试系统

一个基于 Spring Boot + Vue 3 的在线练习题与考试系统，支持五种题型、错题本、自动判分等功能。

## � 技术栈

- **Frontend**: Vue 3 + Vue Router + Bootstrap 5
- **Backend**: Spring Boot 3.2 + Spring Security + JWT + MyBatis-Plus
- **Database**: PostgreSQL 15

## 启动指南 (How to Run)

1. 确保 Docker 已启动
2. 在项目外层目录准备 `repo/` 源码目录后执行：
   ```bash
   docker build -t exam-system -f Dockerfile .
   docker run --rm -p 3000:3000 -p 8000:8000 exam-system
   ```
3. 等待日志出现 `Frontend available at http://localhost:3000`

## 🔗 服务地址 (Services)

| 服务 | 地址 |
|------|------|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8000 |
| Database | 容器内 127.0.0.1:5432 (user: postgres / pass: postgres123) |

## 🧪 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 普通用户 | user | 123456 |

---

## 功能特性

### 用户端
- 🔐 用户注册、登录
- 📝 练习中心 - 按分类随机抽题练习
- 📋 在线考试 - 参加发布的考试，支持计时器
- 📊 考试记录 - 查看历史考试成绩
- 📕 错题本 - 自动收集错题，支持错题练习

### 管理端
- 👥 用户管理 - 用户列表、状态管理
- 📁 分类管理 - 题目分类CRUD
- ❓ 题目管理 - 支持单选、多选、判断、填空、问答五种题型
- 📄 试卷管理 - 组卷、发布、结束考试
- 📈 成绩管理 - 查看所有考试成绩

## 📁 项目结构

```
练习题和考试系统558/
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/com/exam/
│   │   ├── common/         # 公共类
│   │   ├── config/         # 配置类
│   │   ├── controller/     # 控制器
│   │   ├── dto/            # 数据传输对象
│   │   ├── entity/         # 实体类
│   │   ├── exception/      # 异常处理
│   │   ├── mapper/         # MyBatis Mapper
│   │   ├── security/       # 安全配置
│   │   └── service/        # 服务层
│   ├── settings.xml        # Maven 阿里云镜像配置
│   ├── pom.xml
│   └── src/
├── frontend/                # Vue 3 前端
│   ├── src/
│   │   ├── api/            # API 请求
│   │   ├── assets/         # 静态资源
│   │   ├── router/         # 路由配置
│   │   ├── utils/          # 工具函数
│   │   └── views/          # 页面组件
│   ├── nginx.conf          # Nginx 配置
│   ├── package.json
│   └── src/
├── db/
│   └── init.sql            # 数据库初始化脚本
└── README.md
```

## Docker 配置说明

本项目已改为外层单 Dockerfile 构建。该 Dockerfile 会构建 Vue 前端、Spring Boot 后端，并在同一个容器内启动 PostgreSQL、后端服务和 Nginx 前端服务。

### 加速配置

- **npm**: 使用淘宝镜像 `https://registry.npmmirror.com`
- **Maven**: 使用阿里云镜像 `https://maven.aliyun.com/repository/public`

## 📡 API 接口

### 认证
- POST `/api/auth/login` - 登录
- POST `/api/auth/register` - 注册

### 用户
- GET `/api/user/profile` - 获取当前用户信息
- PUT `/api/user/profile` - 更新用户信息
- PUT `/api/user/password` - 修改密码
- GET `/api/user/statistics` - 获取用户统计

### 练习
- GET `/api/categories` - 获取分类列表
- GET `/api/practice/questions` - 获取练习题目
- POST `/api/practice/submit` - 提交练习

### 考试
- GET `/api/exams` - 获取考试列表
- POST `/api/exam/start` - 开始考试
- POST `/api/exam/submit` - 提交考试

### 管理员
- GET `/api/admin/users` - 用户管理
- GET `/api/admin/categories` - 分类管理
- GET `/api/admin/questions` - 题目管理
- GET `/api/admin/exams` - 试卷管理

## License

MIT
