# 数据库初始化

-- 创建库
create database user_db_0;
create database user_db_1;
create database user_db_2;
create database auth_db;
create database log_db;
-- 切换库
use user_db_0;
use user_db_1;
use user_db_2;
use auth_db;
use log_db;
-- 用户表
create table if not exists users
(
    user_id      bigint  comment '用户id' primary key,
    username     varchar(256)                       null comment '登录账号',
    email        VARCHAR(100),
    phone        VARCHAR(20),
    password     varchar(512)                       not null comment '密码',
    user_avatar  varchar(1024)                      null comment '用户头像',
    user_profile varchar(512)                       null comment '用户简介',
    gmt_create   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    gmt_modified datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted   tinyint  default 0                 not null comment '是否删除',
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email),
    UNIQUE KEY uk_phone (phone)
) comment '用户' collate = utf8mb4_general_ci
                 ENGINE = InnoDB;

-- 角色表（权限服务单库）
CREATE TABLE IF NOT EXISTS roles
(
    role_id   INT PRIMARY KEY COMMENT '角色ID（1-超管，2-普通用户，3-管理员）',
    role_code VARCHAR(20) UNIQUE COMMENT '角色编码（super_admin/user/admin）',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称（如"超级管理员"）',
    gmt_create   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    gmt_modified datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted   tinyint  default 0                 not null comment '是否删除'
) COMMENT '角色表' COLLATE = utf8mb4_general_ci
                   ENGINE = InnoDB;


-- 用户-角色关系表
CREATE TABLE IF NOT EXISTS user_roles
(
    id      BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id INT    NOT NULL COMMENT '角色ID',
    UNIQUE KEY uk_user_role (user_id),
    KEY idx_role_id (role_id),
    FOREIGN KEY (role_id) REFERENCES roles (role_id),
    gmt_create   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    gmt_modified datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted   tinyint  default 0                 not null comment '是否删除'
) COMMENT '用户-角色关联表' COLLATE = utf8mb4_general_ci
                            ENGINE = InnoDB;
-- 操作日志表（单库）
CREATE TABLE operation_logs
(
    log_id       BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id      BIGINT,
    action       VARCHAR(50), -- 如 "update_user"
    ip           VARCHAR(15),
    detail       TEXT,        -- 记录修改内容（如 {"field":"email", "old":"a","new":"b"}）
    gmt_create   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    gmt_modified datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted   tinyint  default 0                 not null comment '是否删除'
);



