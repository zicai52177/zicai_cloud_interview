-- Product Service Initial Schema
-- V1__init.sql

CREATE TABLE IF NOT EXISTS `banner` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `location` VARCHAR(64) DEFAULT NULL COMMENT '位置: 页面-位置-模块',
    `name` VARCHAR(128) DEFAULT NULL COMMENT '名称',
    `img_url` VARCHAR(1024) DEFAULT NULL COMMENT '图片URL(逗号分隔)',
    `pc_link` VARCHAR(1024) DEFAULT NULL COMMENT 'PC端链接(逗号分隔)',
    `text` VARCHAR(512) DEFAULT NULL COMMENT '文案',
    `status` VARCHAR(16) NOT NULL DEFAULT 'ON' COMMENT '状态: ON/OFF',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图表';

CREATE TABLE IF NOT EXISTS `benefit` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(128) NOT NULL COMMENT '权益名称',
    `benefit_code` VARCHAR(64) NOT NULL COMMENT '权益编码',
    `description` VARCHAR(512) DEFAULT NULL COMMENT '描述',
    `unit_price` DECIMAL(10,2) DEFAULT NULL COMMENT '单价',
    `valid_days` INT DEFAULT NULL COMMENT '有效天数',
    `is_hot` TINYINT(1) DEFAULT 0 COMMENT '是否热门',
    `is_recommended` TINYINT(1) DEFAULT 0 COMMENT '是否推荐',
    `status` VARCHAR(16) NOT NULL DEFAULT 'ON' COMMENT '状态: ON/OFF',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_benefit_code` (`benefit_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权益目录表';

CREATE TABLE IF NOT EXISTS `product_package` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(128) NOT NULL COMMENT '套餐名称',
    `description` VARCHAR(512) DEFAULT NULL COMMENT '描述',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
    `valid_days` INT DEFAULT NULL COMMENT '有效天数',
    `is_recommended` TINYINT(1) DEFAULT 0 COMMENT '是否推荐',
    `status` VARCHAR(16) NOT NULL DEFAULT 'ON' COMMENT '状态: ON/OFF',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品套餐表';

CREATE TABLE IF NOT EXISTS `package_benefit` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `product_package_id` BIGINT NOT NULL COMMENT '套餐ID',
    `benefit_code` VARCHAR(64) NOT NULL COMMENT '权益编码',
    `count` INT NOT NULL DEFAULT 1 COMMENT '包含次数',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_package_id` (`product_package_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='套餐权益关联表';

CREATE TABLE IF NOT EXISTS `product_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `account_id` BIGINT NOT NULL COMMENT '用户ID',
    `order_type` VARCHAR(32) DEFAULT NULL COMMENT '订单类型: package/benefit',
    `title` VARCHAR(256) DEFAULT NULL COMMENT '订单标题',
    `product_package_id` BIGINT DEFAULT NULL COMMENT '套餐ID',
    `benefit_id` BIGINT DEFAULT NULL COMMENT '权益ID',
    `purchase_count` INT DEFAULT 1 COMMENT '购买数量',
    `unit_price` DECIMAL(10,2) DEFAULT NULL COMMENT '单价',
    `discount` DECIMAL(10,2) DEFAULT NULL COMMENT '折扣',
    `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    `order_state` VARCHAR(16) NOT NULL DEFAULT 'NEW' COMMENT '状态: NEW/PAY/CANCEL/REFUND',
    `pay_type` VARCHAR(32) DEFAULT NULL COMMENT '支付方式: WECHAT_PAY/ALIPAY',
    `transaction_no` VARCHAR(128) DEFAULT NULL COMMENT '第三方交易号',
    `out_trade_no` VARCHAR(64) NOT NULL COMMENT '商户订单号',
    `notify_time` DATETIME DEFAULT NULL COMMENT '支付回调时间',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_out_trade_no` (`out_trade_no`),
    KEY `idx_account_id` (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品订单表';
