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
    user_id      bigint comment '用户id' primary key,
    username     varchar(256)                       null comment '登录账号',
    email        VARCHAR(100)                       null comment '邮箱',
    phone        VARCHAR(20)                        null comment '手机号',
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

# AT模式需要使用的表(无论是否分库分表，只要是分库就必须在所有的业务库中创建该表)
CREATE TABLE `undo_log`
(
    `id`            bigint(20)   NOT NULL AUTO_INCREMENT,
    `branch_id`     bigint(20)   NOT NULL COMMENT 'branch transaction id',
    `xid`           varchar(100) NOT NULL COMMENT 'global transaction id',
    `context`       varchar(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    `rollback_info` longblob     NOT NULL COMMENT 'rollback info',
    `log_status`    int(11)      NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created`   datetime     NOT NULL COMMENT 'create datetime',
    `log_modified`  datetime     NOT NULL COMMENT 'modify datetime',
    `ext`           varchar(100) DEFAULT NULL COMMENT 'ext info',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8;

-- =========   注意区分是否tcc_fence_log分库分表，不分库分表，数据库位于00库下即可 ========

--  不分库分表，数据库位于00库下，性能不够，可考虑分库分表
-- TCC 模式需要使用的表
CREATE TABLE IF NOT EXISTS `tcc_fence_log`
(
    `xid`          VARCHAR(128) NOT NULL COMMENT 'global id',
    `branch_id`    BIGINT       NOT NULL COMMENT 'branch id',
    `action_name`  VARCHAR(64)  NOT NULL COMMENT 'action name',
    `status`       TINYINT      NOT NULL COMMENT 'status(tried:1;committed:2;rollbacked:3;suspended:4)',
    `gmt_create`   DATETIME(3)  NOT NULL COMMENT 'create time',
    `gmt_modified` DATETIME(3)  NOT NULL COMMENT 'update time',
    PRIMARY KEY (`xid`, `branch_id`),
    KEY `idx_gmt_modified` (`gmt_modified`),
    KEY `idx_status` (`status`)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4;

