-- 练习题与考试系统数据库初始化脚本
-- 设置编码
SET client_encoding = 'UTF8';

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status INTEGER NOT NULL DEFAULT 1,
    avatar VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建分类表
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    sort_order INTEGER DEFAULT 0,
    status INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建题目表
CREATE TABLE IF NOT EXISTS questions (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES categories(id),
    type VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    answer TEXT NOT NULL,
    analysis TEXT,
    difficulty INTEGER DEFAULT 1,
    score INTEGER DEFAULT 10,
    status INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建题目选项表
CREATE TABLE IF NOT EXISTS question_options (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    option_label VARCHAR(10) NOT NULL,
    option_content TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    sort_order INTEGER DEFAULT 0
);

-- 创建试卷表
CREATE TABLE IF NOT EXISTS exams (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    total_score INTEGER DEFAULT 0,
    pass_score INTEGER NOT NULL,
    duration INTEGER NOT NULL,
    status INTEGER NOT NULL DEFAULT 0,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建试卷题目关联表
CREATE TABLE IF NOT EXISTS exam_questions (
    id BIGSERIAL PRIMARY KEY,
    exam_id BIGINT NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES questions(id),
    score INTEGER DEFAULT 10,
    sort_order INTEGER DEFAULT 0
);

-- 创建练习记录表
CREATE TABLE IF NOT EXISTS practice_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    category_id BIGINT REFERENCES categories(id),
    mode VARCHAR(20) DEFAULT 'NORMAL',
    total_count INTEGER DEFAULT 0,
    correct_count INTEGER DEFAULT 0,
    score INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建练习答案表
CREATE TABLE IF NOT EXISTS practice_answers (
    id BIGSERIAL PRIMARY KEY,
    record_id BIGINT NOT NULL REFERENCES practice_records(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES questions(id),
    user_answer TEXT,
    is_correct BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建考试记录表
CREATE TABLE IF NOT EXISTS exam_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    exam_id BIGINT NOT NULL REFERENCES exams(id),
    score INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ONGOING',
    start_time TIMESTAMP,
    end_time TIMESTAMP
);

-- 创建考试答案表
CREATE TABLE IF NOT EXISTS exam_answers (
    id BIGSERIAL PRIMARY KEY,
    record_id BIGINT NOT NULL REFERENCES exam_records(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES questions(id),
    user_answer TEXT,
    is_correct BOOLEAN DEFAULT FALSE,
    score INTEGER DEFAULT 0
);

-- 创建错题本表
CREATE TABLE IF NOT EXISTS wrong_questions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    question_id BIGINT NOT NULL REFERENCES questions(id),
    source_type VARCHAR(20),
    source_id BIGINT,
    wrong_count INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, question_id)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_questions_category ON questions(category_id);
CREATE INDEX IF NOT EXISTS idx_questions_type ON questions(type);
CREATE INDEX IF NOT EXISTS idx_exam_questions_exam ON exam_questions(exam_id);
CREATE INDEX IF NOT EXISTS idx_practice_records_user ON practice_records(user_id);
CREATE INDEX IF NOT EXISTS idx_exam_records_user ON exam_records(user_id);
CREATE INDEX IF NOT EXISTS idx_wrong_questions_user ON wrong_questions(user_id);

-- ============================================
-- 初始化数据
-- ============================================

-- 插入管理员用户 (密码: 123456, BCrypt加密)
INSERT INTO users (username, password, nickname, email, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EG', '系统管理员', 'admin@example.com', 'ADMIN', 1),
('user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EG', '测试用户', 'user@example.com', 'USER', 1)
ON CONFLICT (username) DO NOTHING;

-- 插入分类
INSERT INTO categories (id, name, description, sort_order, status) VALUES
(1, 'Java基础', 'Java编程语言基础知识，包括面向对象、集合框架、异常处理等', 1, 1),
(2, '数据库', '数据库原理与SQL语言，包括增删改查、事务、索引等', 2, 1),
(3, '前端开发', 'HTML、CSS、JavaScript等前端技术', 3, 1),
(4, '计算机网络', '网络协议与通信原理，包括TCP/IP、HTTP等', 4, 1),
(5, '操作系统', '操作系统原理与实践，包括进程管理、内存管理等', 5, 1)
ON CONFLICT (name) DO NOTHING;

-- 重置序列
SELECT setval('categories_id_seq', 10);

-- ============================================
-- Java基础题目 (10道)
-- ============================================

-- 单选题1
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(1, 1, 'SINGLE', 'Java中，下列哪个关键字用于定义接口？', 'B', 'interface是Java中用于定义接口的关键字。class用于定义类，abstract用于修饰抽象类或抽象方法，implements用于实现接口。', 1, 5, 1);

INSERT INTO question_options (question_id, option_label, option_content, is_correct, sort_order) VALUES
(1, 'A', 'class', FALSE, 1),
(1, 'B', 'interface', TRUE, 2),
(1, 'C', 'abstract', FALSE, 3),
(1, 'D', 'implements', FALSE, 4);

-- 单选题2
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(2, 1, 'SINGLE', '下列哪个不是Java的基本数据类型？', 'D', 'Java的8种基本数据类型是：byte、short、int、long、float、double、char、boolean。String是引用类型，不是基本数据类型。', 1, 5, 1);

INSERT INTO question_options (question_id, option_label, option_content, is_correct, sort_order) VALUES
(2, 'A', 'int', FALSE, 1),
(2, 'B', 'boolean', FALSE, 2),
(2, 'C', 'char', FALSE, 3),
(2, 'D', 'String', TRUE, 4);

-- 单选题3
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(3, 1, 'SINGLE', 'Java中用于实现运行时多态的机制是？', 'C', '方法重写(Override)是实现运行时多态的主要机制，子类可以重写父类的方法，在运行时根据实际对象类型调用相应的方法实现。', 2, 5, 1);

INSERT INTO question_options (question_id, option_label, option_content, is_correct, sort_order) VALUES
(3, 'A', '方法重载(Overload)', FALSE, 1),
(3, 'B', '构造函数', FALSE, 2),
(3, 'C', '方法重写(Override)', TRUE, 3),
(3, 'D', '静态方法', FALSE, 4);

-- 单选题4
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(4, 1, 'SINGLE', '在Java中，以下哪个修饰符使变量只能被同一个包中的类访问？', 'B', 'default（不写修饰符）表示包访问权限，只能被同一个包中的类访问。public可被所有类访问，private只能在本类中访问，protected可被同包和子类访问。', 2, 5, 1);

INSERT INTO question_options (question_id, option_label, option_content, is_correct, sort_order) VALUES
(4, 'A', 'public', FALSE, 1),
(4, 'B', 'default（不写修饰符）', TRUE, 2),
(4, 'C', 'private', FALSE, 3),
(4, 'D', 'protected', FALSE, 4);

-- 单选题5
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(5, 1, 'SINGLE', 'Java中，用于强制垃圾回收的方法是？', 'B', 'System.gc()方法可以建议JVM进行垃圾回收，但不能保证立即执行。Runtime.gc()也有同样的效果。', 1, 5, 1);

INSERT INTO question_options (question_id, option_label, option_content, is_correct, sort_order) VALUES
(5, 'A', 'System.exit()', FALSE, 1),
(5, 'B', 'System.gc()', TRUE, 2),
(5, 'C', 'System.out()', FALSE, 3),
(5, 'D', 'System.in()', FALSE, 4);

-- 多选题1
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(6, 1, 'MULTIPLE', 'Java中以下哪些是面向对象的特性？（多选）', 'A,B,C,D', '面向对象的四大特性是：封装（Encapsulation）、继承（Inheritance）、多态（Polymorphism）、抽象（Abstraction）。', 1, 10, 1);

INSERT INTO question_options (question_id, option_label, option_content, is_correct, sort_order) VALUES
(6, 'A', '封装', TRUE, 1),
(6, 'B', '继承', TRUE, 2),
(6, 'C', '多态', TRUE, 3),
(6, 'D', '抽象', TRUE, 4);

-- 多选题2
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(7, 1, 'MULTIPLE', '下列关于Java异常处理的说法正确的是？（多选）', 'A,C', 'try块后可以只有finally块没有catch块；一个try块可以有多个catch块；finally块中的代码在大多数情况下都会执行（除非JVM退出）。', 2, 10, 1);

INSERT INTO question_options (question_id, option_label, option_content, is_correct, sort_order) VALUES
(7, 'A', 'try块后可以只有finally块没有catch块', TRUE, 1),
(7, 'B', '一个try块只能有一个catch块', FALSE, 2),
(7, 'C', 'finally块中的代码总会执行', TRUE, 3),
(7, 'D', 'catch块必须在finally块之后', FALSE, 4);

-- 判断题1
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(8, 1, 'JUDGE', 'Java中的String类是不可变的（immutable）。', 'TRUE', 'String类是不可变类，一旦创建，其值就不能被改变。每次看似修改String的操作实际上都是创建了一个新的String对象。这是出于安全性和性能优化的考虑。', 1, 5, 1);

-- 判断题2
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(9, 1, 'JUDGE', 'Java中一个类可以同时继承多个类。', 'FALSE', 'Java不支持类的多重继承，一个类只能继承一个父类（单继承）。但Java支持接口的多重实现，一个类可以实现多个接口。', 1, 5, 1);

-- 判断题3
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(10, 1, 'JUDGE', 'ArrayList是线程安全的集合类。', 'FALSE', 'ArrayList不是线程安全的。如果需要线程安全的List，可以使用Vector、Collections.synchronizedList()或CopyOnWriteArrayList。', 2, 5, 1);

-- ============================================
-- 数据库题目 (8道)
-- ============================================

-- 单选题1
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(11, 2, 'SINGLE', 'SQL中用于删除表中所有数据但保留表结构的命令是？', 'B', 'TRUNCATE用于删除表中所有数据但保留表结构，速度比DELETE快。DELETE也可删除数据但可带条件，DROP会删除整个表。', 1, 5, 1);

INSERT INTO question_options (question_id, option_label, option_content, is_correct, sort_order) VALUES
(11, 'A', 'DELETE', FALSE, 1),
(11, 'B', 'TRUNCATE', TRUE, 2),
(11, 'C', 'DROP', FALSE, 3),
(11, 'D', 'REMOVE', FALSE, 4);

-- 单选题2
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(12, 2, 'SINGLE', '数据库中，索引的主要作用是？', 'A', '索引的主要作用是加速查询，通过建立数据的快速访问路径来提高查询效率。但索引也会占用存储空间，并可能降低插入和更新的速度。', 1, 5, 1);

INSERT INTO question_options (question_id, option_label, option_content, is_correct, sort_order) VALUES
(12, 'A', '加速查询', TRUE, 1),
(12, 'B', '节省存储空间', FALSE, 2),
(12, 'C', '保证数据唯一', FALSE, 3),
(12, 'D', '加速插入', FALSE, 4);

-- 单选题3
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(13, 2, 'SINGLE', 'SQL中，用于查询前10条记录的关键字是？', 'C', '不同数据库有不同的语法：MySQL使用LIMIT，SQL Server使用TOP，Oracle使用ROWNUM。PostgreSQL使用LIMIT。', 1, 5, 1);

INSERT INTO question_options (question_id, option_label, option_content, is_correct, sort_order) VALUES
(13, 'A', 'TOP', FALSE, 1),
(13, 'B', 'FIRST', FALSE, 2),
(13, 'C', 'LIMIT', TRUE, 3),
(13, 'D', 'ROWNUM', FALSE, 4);

-- 多选题1
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(14, 2, 'MULTIPLE', '以下哪些是数据库事务的ACID特性？（多选）', 'A,B,C,D', 'ACID是事务的四大特性：原子性(Atomicity)、一致性(Consistency)、隔离性(Isolation)、持久性(Durability)。', 2, 10, 1);

INSERT INTO question_options (question_id, option_label, option_content, is_correct, sort_order) VALUES
(14, 'A', '原子性(Atomicity)', TRUE, 1),
(14, 'B', '一致性(Consistency)', TRUE, 2),
(14, 'C', '隔离性(Isolation)', TRUE, 3),
(14, 'D', '持久性(Durability)', TRUE, 4);

-- 多选题2
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(15, 2, 'MULTIPLE', '以下哪些是SQL的聚合函数？（多选）', 'A,B,C,D', 'COUNT、SUM、AVG、MAX、MIN都是SQL的聚合函数，用于对一组值进行计算并返回单个值。', 1, 10, 1);

INSERT INTO question_options (question_id, option_label, option_content, is_correct, sort_order) VALUES
(15, 'A', 'COUNT', TRUE, 1),
(15, 'B', 'SUM', TRUE, 2),
(15, 'C', 'AVG', TRUE, 3),
(15, 'D', 'MAX', TRUE, 4);

-- 判断题1
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(16, 2, 'JUDGE', 'LEFT JOIN会返回左表的所有记录，即使右表中没有匹配。', 'TRUE', 'LEFT JOIN（左连接）返回左表的所有记录，对于右表中没有匹配的记录，结果集中右表的列将显示为NULL。', 1, 5, 1);

-- 判断题2
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(17, 2, 'JUDGE', 'WHERE子句可以使用聚合函数进行条件过滤。', 'FALSE', 'WHERE子句不能直接使用聚合函数，需要使用HAVING子句来过滤聚合函数的结果。WHERE在分组前过滤，HAVING在分组后过滤。', 2, 5, 1);

-- 判断题3
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(18, 2, 'JUDGE', '主键(PRIMARY KEY)默认具有唯一性约束且不允许为NULL。', 'TRUE', '主键是表中唯一标识每条记录的字段，它必须是唯一的且不能为NULL。一个表只能有一个主键。', 1, 5, 1);

-- ============================================
-- 前端开发题目 (8道)
-- ============================================

-- 单选题1
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(19, 3, 'SINGLE', 'CSS中用于设置块级元素水平居中的属性组合是？', 'A', 'margin: 0 auto可以使块级元素水平居中，前提是元素需要设置宽度。0表示上下外边距，auto表示左右自动计算实现居中。', 1, 5, 1);

INSERT INTO question_options (question_id, option_label, option_content, is_correct, sort_order) VALUES
(19, 'A', 'margin: 0 auto', TRUE, 1),
(19, 'B', 'padding: 0 auto', FALSE, 2),
(19, 'C', 'text-align: center', FALSE, 3),
(19, 'D', 'float: center', FALSE, 4);

-- 单选题2
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(20, 3, 'SINGLE', 'JavaScript中用于声明变量且不可重新赋值的关键字是？', 'C', 'const声明的变量不可重新赋值（但对象的属性可以修改）。let和var声明的变量可以重新赋值，let具有块级作用域。', 1, 5, 1);

INSERT INTO question_options (question_id, option_label, option_content, is_correct, sort_order) VALUES
(20, 'A', 'var', FALSE, 1),
(20, 'B', 'let', FALSE, 2),
(20, 'C', 'const', TRUE, 3),
(20, 'D', 'function', FALSE, 4);

-- 单选题3
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(21, 3, 'SINGLE', 'HTML5中，用于定义页面主要内容区域的语义化标签是？', 'B', '<main>标签用于定义文档的主要内容区域，一个页面中只能有一个。<header>用于页眉，<footer>用于页脚，<section>用于内容分区。', 1, 5, 1);

INSERT INTO question_options (question_id, option_label, option_content, is_correct, sort_order) VALUES
(21, 'A', '<header>', FALSE, 1),
(21, 'B', '<main>', TRUE, 2),
(21, 'C', '<section>', FALSE, 3),
(21, 'D', '<footer>', FALSE, 4);

-- 多选题1
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(22, 3, 'MULTIPLE', 'Vue.js的生命周期钩子函数包括以下哪些？（多选）', 'A,B,C', 'Vue.js的生命周期钩子包括created、mounted、updated、destroyed等。render不是生命周期钩子，它是渲染函数。', 2, 10, 1);

INSERT INTO question_options (question_id, option_label, option_content, is_correct, sort_order) VALUES
(22, 'A', 'created', TRUE, 1),
(22, 'B', 'mounted', TRUE, 2),
(22, 'C', 'updated', TRUE, 3),
(22, 'D', 'render', FALSE, 4);

-- 多选题2
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(23, 3, 'MULTIPLE', '以下哪些是CSS的定位方式？（多选）', 'A,B,C,D', 'CSS的position属性可以设置为：static（默认）、relative（相对）、absolute（绝对）、fixed（固定）、sticky（粘性）等。', 1, 10, 1);

INSERT INTO question_options (question_id, option_label, option_content, is_correct, sort_order) VALUES
(23, 'A', 'relative', TRUE, 1),
(23, 'B', 'absolute', TRUE, 2),
(23, 'C', 'fixed', TRUE, 3),
(23, 'D', 'sticky', TRUE, 4);

-- 判断题1
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(24, 3, 'JUDGE', 'HTML5中，localStorage的数据会在浏览器关闭后自动清除。', 'FALSE', 'localStorage的数据会永久保存在浏览器中，除非手动清除或通过代码删除。sessionStorage的数据才会在浏览器关闭后清除。', 1, 5, 1);

-- 判断题2
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(25, 3, 'JUDGE', 'JavaScript是一种强类型语言。', 'FALSE', 'JavaScript是一种弱类型（或动态类型）语言，变量的类型可以在运行时改变。TypeScript是JavaScript的强类型超集。', 1, 5, 1);

-- 判断题3
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(26, 3, 'JUDGE', 'CSS中，id选择器的优先级高于class选择器。', 'TRUE', 'CSS选择器优先级从高到低：!important > 内联样式 > ID选择器 > 类选择器/属性选择器/伪类 > 元素选择器/伪元素。', 1, 5, 1);

-- ============================================
-- 计算机网络题目 (6道)
-- ============================================

-- 单选题1
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(27, 4, 'SINGLE', 'HTTP协议默认使用的端口号是？', 'B', 'HTTP协议默认使用80端口，HTTPS默认使用443端口，FTP默认使用21端口，SSH默认使用22端口。', 1, 5, 1);

INSERT INTO question_options (question_id, option_label, option_content, is_correct, sort_order) VALUES
(27, 'A', '8080', FALSE, 1),
(27, 'B', '80', TRUE, 2),
(27, 'C', '443', FALSE, 3),
(27, 'D', '21', FALSE, 4);

-- 单选题2
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(28, 4, 'SINGLE', 'TCP协议和UDP协议的主要区别是？', 'A', 'TCP是面向连接的可靠传输协议，而UDP是无连接的不可靠传输协议。TCP保证数据完整有序，UDP速度快但不保证。', 2, 5, 1);

INSERT INTO question_options (question_id, option_label, option_content, is_correct, sort_order) VALUES
(28, 'A', 'TCP是面向连接的，UDP是无连接的', TRUE, 1),
(28, 'B', 'TCP速度更快', FALSE, 2),
(28, 'C', 'UDP更可靠', FALSE, 3),
(28, 'D', '两者没有区别', FALSE, 4);

-- 多选题1
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(29, 4, 'MULTIPLE', 'OSI七层模型包括以下哪些层？（多选）', 'A,B,C,D', 'OSI七层模型从下到上：物理层、数据链路层、网络层、传输层、会话层、表示层、应用层。', 2, 10, 1);

INSERT INTO question_options (question_id, option_label, option_content, is_correct, sort_order) VALUES
(29, 'A', '物理层', TRUE, 1),
(29, 'B', '网络层', TRUE, 2),
(29, 'C', '传输层', TRUE, 3),
(29, 'D', '应用层', TRUE, 4);

-- 判断题1
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(30, 4, 'JUDGE', 'IP地址由32位二进制数组成。', 'TRUE', 'IPv4地址由32位二进制数组成，通常表示为4个0-255的十进制数，用点分隔。IPv6地址由128位组成。', 1, 5, 1);

-- 判断题2
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(31, 4, 'JUDGE', 'HTTPS协议比HTTP协议更安全，因为它使用了SSL/TLS加密。', 'TRUE', 'HTTPS在HTTP的基础上加入了SSL/TLS协议，对传输的数据进行加密，保护数据不被窃取或篡改。', 1, 5, 1);

-- 判断题3
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(32, 4, 'JUDGE', 'DNS的主要作用是将IP地址转换为域名。', 'FALSE', 'DNS（域名系统）的主要作用是将域名转换为IP地址，即域名解析。反向DNS才是将IP地址转换为域名。', 1, 5, 1);

-- ============================================
-- 操作系统题目 (6道)
-- ============================================

-- 单选题1
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(33, 5, 'SINGLE', '操作系统中，进程和线程的主要区别是？', 'A', '进程是资源分配的基本单位，拥有独立的地址空间；线程是CPU调度的基本单位，共享进程的资源。线程开销更小。', 2, 5, 1);

INSERT INTO question_options (question_id, option_label, option_content, is_correct, sort_order) VALUES
(33, 'A', '进程是资源分配单位，线程是CPU调度单位', TRUE, 1),
(33, 'B', '线程拥有独立地址空间', FALSE, 2),
(33, 'C', '进程比线程开销小', FALSE, 3),
(33, 'D', '两者没有区别', FALSE, 4);

-- 单选题2
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(34, 5, 'SINGLE', '常见的进程调度算法不包括以下哪个？', 'D', '常见的进程调度算法有：先来先服务(FCFS)、短作业优先(SJF)、时间片轮转(RR)、优先级调度等。冒泡排序是排序算法，不是调度算法。', 1, 5, 1);

INSERT INTO question_options (question_id, option_label, option_content, is_correct, sort_order) VALUES
(34, 'A', '先来先服务(FCFS)', FALSE, 1),
(34, 'B', '短作业优先(SJF)', FALSE, 2),
(34, 'C', '时间片轮转(RR)', FALSE, 3),
(34, 'D', '冒泡排序', TRUE, 4);

-- 多选题1
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(35, 5, 'MULTIPLE', '以下哪些是常见的页面置换算法？（多选）', 'A,B,C,D', '常见的页面置换算法有：最佳置换(OPT)、先进先出(FIFO)、最近最少使用(LRU)、时钟算法(CLOCK)等。', 2, 10, 1);

INSERT INTO question_options (question_id, option_label, option_content, is_correct, sort_order) VALUES
(35, 'A', '先进先出(FIFO)', TRUE, 1),
(35, 'B', '最近最少使用(LRU)', TRUE, 2),
(35, 'C', '最佳置换(OPT)', TRUE, 3),
(35, 'D', '时钟算法(CLOCK)', TRUE, 4);

-- 判断题1
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(36, 5, 'JUDGE', '死锁是指两个或多个进程互相等待对方释放资源而无限等待的状态。', 'TRUE', '死锁是指两个或多个进程互相持有对方需要的资源，同时又等待对方释放资源，导致所有进程都无法继续执行。', 2, 5, 1);

-- 判断题2
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(37, 5, 'JUDGE', '虚拟内存可以使程序运行的内存空间大于物理内存。', 'TRUE', '虚拟内存技术通过将部分数据存储在磁盘上，使程序可以使用比物理内存更大的地址空间，实现内存的按需加载。', 2, 5, 1);

-- 判断题3
INSERT INTO questions (id, category_id, type, content, answer, analysis, difficulty, score, status) VALUES
(38, 5, 'JUDGE', '操作系统的内核运行在用户态。', 'FALSE', '操作系统的内核运行在内核态（核心态），具有最高权限。用户程序运行在用户态，权限受限，需要通过系统调用访问内核服务。', 1, 5, 1);

-- 重置问题序列
SELECT setval('questions_id_seq', 50);

-- ============================================
-- 插入示例试卷
-- ============================================

INSERT INTO exams (id, title, description, total_score, pass_score, duration, status) VALUES
(1, 'Java基础测试', 'Java编程语言基础知识测试，涵盖面向对象、异常处理等核心概念', 0, 60, 60, 1),
(2, '数据库基础测试', '数据库原理与SQL语言基础测试，涵盖CRUD、事务、索引等', 0, 30, 30, 1),
(3, '前端开发测试', '前端开发基础测试，涵盖HTML、CSS、JavaScript等', 0, 30, 45, 1);

-- 重置试卷序列
SELECT setval('exams_id_seq', 10);

-- 为Java基础测试添加题目
INSERT INTO exam_questions (exam_id, question_id, score, sort_order) VALUES
(1, 1, 10, 1),
(1, 2, 10, 2),
(1, 3, 10, 3),
(1, 4, 10, 4),
(1, 5, 10, 5),
(1, 6, 15, 6),
(1, 7, 15, 7),
(1, 8, 10, 8),
(1, 9, 10, 9),
(1, 10, 10, 10);

-- 为数据库基础测试添加题目
INSERT INTO exam_questions (exam_id, question_id, score, sort_order) VALUES
(2, 11, 10, 1),
(2, 12, 10, 2),
(2, 13, 10, 3),
(2, 14, 15, 4),
(2, 15, 15, 5),
(2, 16, 10, 6),
(2, 17, 10, 7),
(2, 18, 10, 8);

-- 为前端开发测试添加题目
INSERT INTO exam_questions (exam_id, question_id, score, sort_order) VALUES
(3, 19, 10, 1),
(3, 20, 10, 2),
(3, 21, 10, 3),
(3, 22, 15, 4),
(3, 23, 15, 5),
(3, 24, 10, 6),
(3, 25, 10, 7),
(3, 26, 10, 8);

-- 更新试卷总分
UPDATE exams SET total_score = (
    SELECT COALESCE(SUM(score), 0) FROM exam_questions WHERE exam_id = exams.id
);

-- 完成初始化
SELECT '数据库初始化完成！' AS status;
SELECT '共创建 ' || (SELECT COUNT(*) FROM questions) || ' 道题目' AS info;
SELECT '共创建 ' || (SELECT COUNT(*) FROM exams) || ' 套试卷' AS info;
