-- AI Service Initial Schema
-- V1__init.sql

CREATE TABLE IF NOT EXISTS `resume` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `account_id` BIGINT NOT NULL COMMENT '用户ID',
    `filename` VARCHAR(256) DEFAULT NULL COMMENT '文件名',
    `file_path` VARCHAR(512) DEFAULT NULL COMMENT '文件路径(OSS)',
    `file_type` VARCHAR(16) DEFAULT NULL COMMENT '文件类型: pdf/doc/docx',
    `content` TEXT DEFAULT NULL COMMENT '简历文本内容',
    `skill_tags` VARCHAR(1024) DEFAULT NULL COMMENT '技能标签(逗号分隔)',
    `evaluation` TEXT DEFAULT NULL COMMENT 'AI简历分析',
    `status` VARCHAR(16) NOT NULL DEFAULT 'UPLOADING' COMMENT '状态: UPLOADING/PARSING/PARSED/FAILED',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_account_id` (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历表';

CREATE TABLE IF NOT EXISTS `interview` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(256) DEFAULT NULL COMMENT '面试标题',
    `description` VARCHAR(512) DEFAULT NULL COMMENT '面试描述',
    `type` VARCHAR(32) DEFAULT NULL COMMENT '类型: specialized/in-depth/resume_review',
    `extend_content` TEXT DEFAULT NULL COMMENT '扩展内容',
    `profile` TEXT DEFAULT NULL COMMENT '求职者画像',
    `account_id` BIGINT NOT NULL COMMENT '用户ID',
    `resume_id` BIGINT DEFAULT NULL COMMENT '关联简历ID',
    `status` VARCHAR(32) NOT NULL DEFAULT 'GENERATING' COMMENT '状态: GENERATING/IN_PROGRESS/EVALUATING/COMPLETED',
    `overall_score` DECIMAL(5,2) DEFAULT NULL COMMENT '总分(0-100)',
    `summary` TEXT DEFAULT NULL COMMENT '面试总结',
    `strength` TEXT DEFAULT NULL COMMENT '优势',
    `improvement` TEXT DEFAULT NULL COMMENT '待改进',
    `suggestion` TEXT DEFAULT NULL COMMENT '建议',
    `technical_skills` DECIMAL(5,2) DEFAULT NULL COMMENT '技术能力分',
    `logical_thinking` DECIMAL(5,2) DEFAULT NULL COMMENT '逻辑思维分',
    `communication` DECIMAL(5,2) DEFAULT NULL COMMENT '沟通表达分',
    `problem_solving` DECIMAL(5,2) DEFAULT NULL COMMENT '解决问题分',
    `passed_question` INT DEFAULT 0 COMMENT '及格题数(>=60)',
    `excellent_question` INT DEFAULT 0 COMMENT '优秀题数(>=80)',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_account_id` (`account_id`),
    KEY `idx_resume_id` (`resume_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试表';

CREATE TABLE IF NOT EXISTS `interview_round` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `interview_id` BIGINT NOT NULL COMMENT '面试ID',
    `account_id` BIGINT NOT NULL COMMENT '用户ID',
    `round_number` INT NOT NULL DEFAULT 1 COMMENT '轮次',
    `round_type` VARCHAR(32) DEFAULT NULL COMMENT '轮次类型',
    `status` VARCHAR(32) NOT NULL DEFAULT 'IN_PROGRESS' COMMENT '状态: IN_PROGRESS/EVALUATING/COMPLETED',
    `description` VARCHAR(512) DEFAULT NULL COMMENT '描述',
    `total_question` INT DEFAULT 0 COMMENT '总题数',
    `answered_questions` INT DEFAULT 0 COMMENT '已答题数',
    `overall_score` DECIMAL(5,2) DEFAULT NULL COMMENT '本轮得分',
    `summary` TEXT DEFAULT NULL,
    `strength` TEXT DEFAULT NULL,
    `improvement` TEXT DEFAULT NULL,
    `suggestion` TEXT DEFAULT NULL,
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_interview_id` (`interview_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试轮次表';

CREATE TABLE IF NOT EXISTS `question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `interview_id` BIGINT NOT NULL COMMENT '面试ID',
    `account_id` BIGINT NOT NULL COMMENT '用户ID',
    `interview_round_id` BIGINT DEFAULT NULL COMMENT '轮次ID',
    `status` VARCHAR(16) DEFAULT 'PENDING' COMMENT '状态',
    `user_answer` TEXT DEFAULT NULL COMMENT '用户答案',
    `evaluation` TEXT DEFAULT NULL COMMENT 'AI评价',
    `content` TEXT DEFAULT NULL COMMENT '题目内容',
    `type` VARCHAR(32) DEFAULT NULL COMMENT '题型: CODING/SHORT_ANSWER',
    `difficulty` VARCHAR(16) DEFAULT NULL COMMENT '难度: BEGINNER/INTERMEDIATE/ADVANCED',
    `category` VARCHAR(64) DEFAULT NULL COMMENT '分类',
    `standard_answer` TEXT DEFAULT NULL COMMENT '标准答案',
    `key_points` TEXT DEFAULT NULL COMMENT '关键考点(JSON)',
    `max_score` INT DEFAULT 100 COMMENT '满分',
    `time_limit` INT DEFAULT NULL COMMENT '时限(分钟)',
    `user_score` INT DEFAULT NULL COMMENT '得分',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_interview_id` (`interview_id`),
    KEY `idx_round_id` (`interview_round_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试题目表';

CREATE TABLE IF NOT EXISTS `ai_chat_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `account_id` BIGINT NOT NULL COMMENT '用户ID',
    `interview_id` BIGINT NOT NULL COMMENT '面试ID',
    `record_type` VARCHAR(16) NOT NULL COMMENT '记录类型: METADATA/MESSAGE',
    `role` VARCHAR(16) DEFAULT NULL COMMENT '角色: user/assistant/system',
    `content` TEXT DEFAULT NULL COMMENT '消息内容',
    `sequence` INT DEFAULT 0 COMMENT '消息序号',
    `status` VARCHAR(16) DEFAULT 'ONGOING' COMMENT '会话状态: ONGOING/ENDED',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_interview_id` (`interview_id`),
    KEY `idx_account_id` (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话记录表';
