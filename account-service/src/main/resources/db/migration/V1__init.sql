-- Account Service Initial Schema
-- V1__init.sql

CREATE TABLE IF NOT EXISTS `account` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(128) DEFAULT NULL COMMENT '用户名',
    `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `head_img` VARCHAR(512) DEFAULT NULL COMMENT '头像URL',
    `status` VARCHAR(16) NOT NULL DEFAULT 'ON' COMMENT '状态: ON/OFF',
    `role` VARCHAR(16) NOT NULL DEFAULT 'COMMON' COMMENT '角色: COMMON/ADMIN',
    `openid` VARCHAR(128) DEFAULT NULL COMMENT '微信OpenID',
    `unionid` VARCHAR(128) DEFAULT NULL COMMENT '微信UnionID',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户账户表';

CREATE TABLE IF NOT EXISTS `account_benefit` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `account_id` BIGINT NOT NULL COMMENT '用户ID',
    `benefit_id` BIGINT DEFAULT NULL COMMENT '权益ID',
    `benefit_code` VARCHAR(64) NOT NULL COMMENT '权益编码',
    `product_order_id` BIGINT DEFAULT NULL COMMENT '来源订单ID',
    `order_type` VARCHAR(32) DEFAULT NULL COMMENT '订单类型: package_order/benefit_order',
    `remaining_count` INT NOT NULL DEFAULT 0 COMMENT '剩余次数',
    `total_count` INT NOT NULL DEFAULT 0 COMMENT '总次数',
    `start_time` DATETIME DEFAULT NULL COMMENT '生效时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '过期时间',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_account_id` (`account_id`),
    KEY `idx_benefit_code` (`benefit_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户权益表';

CREATE TABLE IF NOT EXISTS `benefit_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `account_id` BIGINT NOT NULL COMMENT '用户ID',
    `account_benefit_id` BIGINT NOT NULL COMMENT '用户权益ID',
    `benefit_code` VARCHAR(64) NOT NULL COMMENT '权益编码',
    `use_times` INT NOT NULL DEFAULT 1 COMMENT '使用次数',
    `business_id` VARCHAR(128) DEFAULT NULL COMMENT '业务ID',
    `message_id` VARCHAR(64) NOT NULL COMMENT '消息唯一ID',
    `lock_state` VARCHAR(16) NOT NULL DEFAULT 'LOCK' COMMENT '状态: LOCK/FINISH/CANCEL',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_message_id` (`message_id`),
    KEY `idx_account_id` (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权益任务表(本地事务消息)';

CREATE TABLE IF NOT EXISTS `benefit_usage_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `account_id` BIGINT NOT NULL COMMENT '用户ID',
    `biz_id` BIGINT DEFAULT NULL COMMENT '业务ID',
    `benefit_code` VARCHAR(64) NOT NULL COMMENT '权益编码',
    `account_benefit` BIGINT DEFAULT NULL COMMENT '用户权益ID',
    `usage_time` DATETIME DEFAULT NULL COMMENT '使用时间',
    `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_account_id` (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权益使用日志';
