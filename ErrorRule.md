# Error Rules & Best Practices

## 1. Docker Environment

### Database Connection Race Condition
- **Context**: When using `docker-compose`, the database service may report "Healthy" or "Started" before it is actually ready to accept connections.
- **Rule**: The backend MUST implement a **connection retry mechanism** (with backoff) on startup. Do NOT rely solely on `depends_on` or `healthcheck` in `docker-compose.yml`.
- **Example**:
  ```java
  // Bad: Fail immediately on start
  // Good: Loop with Thread.sleep() catching connection exceptions
  for (int i = 0; i < 10; i++) {
      try { initializeData(); break; } 
      catch (Exception e) { Thread.sleep(3000); }
  }
  ```

### Docker Compose Syntax
- **Rule**: Do not include the `version: 'x.x'` top-level property in `docker-compose.yml`. It is obsolete in modern Docker Compose specifications.

## 2. Database & Data Initialization

### Character Encoding
- **Context**: Inserting non-ASCII (e.g., Chinese) characters via `init.sql` often leads to garbled text (`?` or `乱码`) depending on the OS/Container locale.
- **Rule**: For **initial test data**, prioritize using **English**. If multilingual support is strictly required, ensure strict UTF-8 configuration across:
  1. Dockerfile: `ENV LANG=C.UTF-8`
  2. JDBC URL: `jdbc:mysql://...?useUnicode=true&characterEncoding=utf8`
  3. MySQL Configuration: `[mysqld] character-set-server=utf8mb4`

### Password Initialization
- **Context**: Hardcoding BCrypt hashes in SQL scripts (e.g., `$2a$10$...`) is brittle. Changing the algorithm or salt rounds breaks the data.
- **Rule**: Use a **programmatic approach** (e.g., Spring Boot `CommandLineRunner`) to create default users.
- **Benefit**: Ensures passwords are hashed using the *exact same* `PasswordEncoder` bean running in the application.

## 3. API Design & Security

### Admin Self-Lockout Prevention
- **Context**: Admins often accidentally disable their own account or remove the last administrator, rendering the system unmanageable.
- **Rule**: In User Update/Delete APIs, strictly enforce:
  1. **Self-Protection**: Current user CANNOT disable/delete themselves.
  2. **Last Admin Protection**: If the target user is an Admin, check if they are the *last* enabled admin before allowing disable/delete.

### Partial Updates vs. Full Overwrite
- **Context**: Using a full Entity (e.g., `User`) object for partial updates (e.g., "Update Profile") is dangerous. If the frontend sends `{ name, email }` but omits `role`, the backend might overwrite `role` to `null`.
- **Rule**: 
  - Use specific **DTOs** (e.g., `UpdateProfileRequest`) matching the intent.
  - Or create **separate endpoints** (e.g., `PUT /users/{id}/profile`, `PUT /users/{id}/password`) rather than a single generic `PUT /users/{id}`.

### Role-Based Authorization
- **Rule**: Explicitly test endpoint permissions. Ensure `DELETE` and `POST` (Create) operations are protected by `@PreAuthorize("hasRole('ADMIN')")`.
- **UI Logic**: Frontend must strictly hide buttons (Delete/Edit) based on the user's role to confirm with backend restrictions.

## 4. Frontend & State Management

### Permission Persistence & Data Sync
- **Context**: Merging new user data with old state (e.g., `user.value = { ...user.value, ...data }`) is risky. If a user switches accounts (Admin -> User), stale fields (like `roleCode`) may persist if the new API response omits them.
- **Rule**: When updating user state after login or profile refresh, use **full replacement** or **explicit field mapping**. Never assume a partial merge is safe for security-critical fields like `roleCode`.

### Non-Admin Statistics Handling
- **Context**: Dashboards often fail for non-admin users if the API calls are guarded by `isAdmin` checks but the UI still tries to render statistical widgets.
- **Rule**:
  1. **UI Guarding**: Wrap admin-only widgets (charts, stats cards) with `v-if="isAdmin"`.
  2. **API Guarding**: In data fetching logic, explicitly check permissions before making admin-only API calls to avoid 403 errors or empty data processing.

## 5. Development Environment & Tooling

### Windows PowerShell & SQL Escaping
- **Context**: Running `docker exec ... mysql -e "UPDATE ..."` with BCrypt hashes (containing `$`) in PowerShell causes silent data corruption because `$` is interpreted as a variable.
- **Rule**: 
  1. Use **SQL files** (`docker cp file.sql ...`) for complex updates.
  2. Or strictly escape `$` as `` `$ `` (backtick dollar) in PowerShell.
  3. Or use **Single Quotes** `'...'` for the SQL string if possible (though PowerShell variable expansion rules can be tricky).

### JDK 24+ Compatibility
- **Context**: Using "Latest" or "Edge" JDK versions (e.g., JDK 24) requires a synchronized upgrade of the entire toolchain.
- **Rule**:
  - **Lombok**: Must be updated (e.g., 1.18.36+ or 1.18.38) for JDK 24 support.
  - **Spring Boot**: Must use compatible versions (e.g., 3.4.x+).
  - **Docker**: Prefer standard/mainstream base images (e.g., `eclipse-temurin:24-jre`) over older Alpine variants if compatibility issues arise.



# 项目开发避坑指南 (Error Rules & Best Practices)

本文档总结了本项目开发过程中遇到的典型问题与解决方案，供后续开发参考。

## 1. 数据库与 Docker 配置
### 1.1 JDBC 字符编码问题
- **错误**：`jdbc:mysql://...?characterEncoding=utf8mb4`
- **原因**：Java JDBC 驱动不识别 `utf8mb4` 这种 MySQL 特有的字符集名称。Java 中对应的编码标准名称是 `UTF-8`。
- **正确**：`jdbc:mysql://...?characterEncoding=UTF-8`
- **注意**：数据库端的字符集设置（`CREATE DATABASE ... CHARSET utf8mb4`）仍然应该使用 `utf8mb4` 以支持 Emoji，但 JDBC 连接字符串必须用 `UTF-8`。

### 1.2 Docker MySQL 端口冲突
- **错误**：Docker Compose 中映射 `3306:3306` 启动失败。
- **原因**：开发机本地通常已运行 MySQL 占用了 3306 端口。
- **建议**：Docker 容器映射端口时使用非标准端口，如 `3307:3306` `3308:3306`，避免与宿主机服务冲突。

### 1.3 数据库启动时序
- **现象**：后端服务启动报错 `LinkFailureException` 或连接拒绝。
- **原因**：MySQL 容器完成初始化的时间通常长于后端应用启动时间。
- **解决**：在 `docker-compose.yml` 中配置完善的 `healthcheck`（增加重试次数和 `start_period`）以及 `depends_on: condition: service_healthy`。

## 2. Spring Boot & Security
### 2.1 Spring Security 认证上下文
- **错误**：JWT 过滤器校验通过，但 `@Authenticated` 接口仍报 403 Forbidden。
- **原因**：过滤器中仅设置了 `request.setAttribute`，未将认证信息注入 Spring Security 上下文。
- **正确**：必须显式调用：
  ```java
  UsernamePasswordAuthenticationToken auth = ...;
  SecurityContextHolder.getContext().setAuthentication(auth);
  ```

### 2.2 Bean 定义冲突
- **错误**：应用启动失败，提示 `BeanDefinitionOverrideException`。
- **原因**：在多个配置类（如 `SecurityConfig` 和 `PasswordConfig`）中定义了同名的 Bean（如 `PasswordEncoder`）。
- **解决**：保持 Bean 定义唯一，或在 `application.yml` 中开启覆盖（不推荐）：`spring.main.allow-bean-definition-overriding=true`。最好的做法是整合配置类，删除冗余定义。

### 2.3 BCrypt 密码哈希
- **风险**：在 `init.sql` 中硬编码 BCrypt 哈希值 (`$2a$10$...`) 容易因库版本差异导致无法验证。
- **最佳实践**：使用 `DataInitializer` (`CommandLineRunner`) 在应用启动时检查并使用当前环境的 `PasswordEncoder` 重新加密/修复密码，确保兼容性。

## 3. MyBatis-Plus
### 3.1 分页类型不匹配
- **错误**：`incompatible types: IPage<T> cannot be converted to Page<T>`
- **原因**：MyBatis-Plus 的 Mapper 方法返回接口类型 `IPage`，而 Service 层试图将其赋值给具体实现类 `Page`。
- **解决**：始终面向接口编程，Service 层和工具类（如 `PageResult.of()`）的参数应声明为 `IPage<T>` 而非 `Page<T>`。

### 4.2 配置注入风险
- **错误**：`Failed to bind properties ... IllegalStateException: The configuration of the pool is sealed`。
- **原因**：尝试通过环境变量（如 `SPRING_DATASOURCE_HIKARI_...`）在运行时修改已初始化的连接池配置。
- **解决**：HikariCP 等连接池配置通常在启动时锁定，应通过 `application.yml` 配置，避免使用不支持动态修改的环境变量注入方式。

## 4. 常见全栈问题与修复 (Common Full-Stack Issues)

### 4.1 SQL 初始化脚本编码陷阱
- **现象**：尽管 JDBC 连接和数据库均设置为 UTF-8，`init.sql` 中的中文数据插入后仍显示为乱码（或双重编码）。
- **原因**：MySQL 客户端（包括 Docker 容器自动运行脚本的客户端）在读取 SQL 文件时可能默认使用 Latin-1 编码，并未识别文件本身的 UTF-8 编码。
- **解决**：在 `init.sql` 文件的**第一行**强制添加 `SET NAMES utf8mb4;`，显式告知客户端文件的字符集。

### 4.2 Spring MVC 日期序列化 (500错误)
- **现象**：控制台报错 `Java 8 date/time type java.time.LocalDateTime not supported by default`，导致后端返回 500。
- **原因**：Spring（特别是非 Boot 环境或旧版本 Jackson）默认不含 `JavaTimeModule`，无法将 `LocalDateTime` 序列化为字符串。
- **解决**：需自定义 `ObjectMapper` Bean，注册 `JavaTimeModule` 并配置 `SimpleDateFormat` 或 `DateTimeFormatter`，然后在 Spring 配置文件 (`spring-mvc.xml`) 中启用该 Mapper。

### 4.3 前端 API 参数传递误区
- **现象**：后端报错 `400 Bad Request` (HttpMessageNotReadableException)，但 DTO 定义看似正确。
- **原因**：前端调用 API 函数时参数格式不匹配。例如函数定义为 `func(a, b)`，由于习惯或误写，调用成了 `func({a, b})`。这导致后端接收到的 JSON 结构层级错误（嵌套了一层对象）。
- **解决**：严格检查前端 API 调用的参数个数与类型，避免将对象当作单参数传递给多参数函数。

### 4.4 静态资源缓存顽疾
- **现象**：修改了 CSS/JS 代码并重启了服务，但浏览器端显示的界面样式依然是旧的（如 hover 效果未去除）。
- **原因**：Nginx 或浏览器对静态文件名相同的资源有强缓存策略。即便容器重建，文件名未变，浏览器可能不请求新文件。
- **解决**：在 HTML 引用静态资源时添加版本号参数（Cache Busting），如 `<link href="style.css?v=2">`，强制浏览器认为资源已更新。

### 4.5 深色模式下的 UI 对比度
- **现象**：在深色主题中，表格行的 `hover` 背景色与某些按钮（如次级按钮）的背景色过于接近，导致悬停时按钮“隐身”。
- **原因**：未充分考虑叠加状态下的颜色对比度。
- **解决**：
  1. 降低表格行悬停的透明度（如使用 `rgba(255,255,255,0.05)` 代替实色）。
  2. 按钮使用 Outline（描边）风格作为默认态，悬停时再填充，增强层次感。

## 5. 项目特定避坑指南 (Project Specific Pitfalls)

### 5.1 数据库名称不一致 (Database Name Mismatch)
- **现象**：`docker-compose.yml` 配置了 `MYSQL_DATABASE: warehouse`，但应用启动报错 `Table 'warehouse.sys_user' doesn't exist`。
- **原因**：`init.sql` 脚本中可能显式执行了 `USE warehouse_db;` 或在错误的 schema 中建表，导致 Docker 创建的数据库是空的。
- **解决**：确保 `docker-compose.yml` 中的数据库名与 `init.sql` 中的 `USE` 语句完全一致。建议移除 `init.sql` 中的建库语句，直接使用 `USE` 指向 Docker 预创建的数据库。

### 5.2 AOP 参数序列化异常 (Serialization Loop)
- **现象**：业务逻辑正常，但请求返回 500 错误，且日志堆栈指向 AOP 切面。
- **原因**：通用日志切面使用 `Arrays.toString()` 记录参数，当参数包含复杂对象（如由 MyBatis-Plus 查询出的包含双向关联或懒加载的对象）时，触发 `StackOverflowError` 或序列化异常。
- **解决**：
  1. 在 AOP 中记录参数时必须包裹 `try-catch`，确保日志错误不影响业务。
  2. 避免对复杂对象调用 `toString()`，改为仅记录 ID 或类名。
  3. 对于 `List` 等集合，仅记录集合大小或前几个元素。

### 5.3 安全上下文 ID 信任原则
- **现象**：用户可以通过修改请求体中的 `id` 字段来篡改其他用户的数据。
- **原因**：后端更新接口（如 `updateProfile`）盲目信任前端传递的 `id`，而非当前登录用户的真实 ID。
- **原则**：对于"修改个人信息"类接口，**完全忽略**前端请求体中的 ID 字段，始终从 `SecurityContext` / `Token` 中获取当前登录用户的 ID 进行操作。

### 5.4 UI 模态框组件截断
- **现象**：在表格内的编辑模态框中，`el-input-number` 的加减按钮显示不全或布局错乱。
- **原因**：表格列宽设置过小（如 100px-120px），无法容纳带有左右按钮的输入框。
- **解决**：
  1. 数字输入列宽至少设置为 **150px**。
  2. 使用 `controls-position="right"` 将加减按钮垂直堆叠在右侧，节省水平空间。
  3. 显式设置组件 `style="width: 100%"` 填满单元格。

### 5.5 搜索过滤体验优化
- **现象**：搜索"用户"无法匹配到"用户管理"模块，或输入必须完全精确。
- **原因**：后端使用 `eq()` (精确匹配) 处理文本搜索字段。
- **最佳实践**：
  1. **后端**：对于文本类检索字段（如模块名、描述），默认使用 `like()` (模糊匹配)。
  2. **前端**：对于有限集合的字段（如系统模块枚举），应放弃文本框，改用 **Select 下拉框**，从源头消除拼写错误和匹配歧义。
