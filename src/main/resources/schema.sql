-- 小区共享车位 数据库初始化脚本
CREATE DATABASE IF NOT EXISTS parking_share DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE parking_share;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `openid` VARCHAR(64) NOT NULL COMMENT '微信openid',
    `nickname` VARCHAR(64) DEFAULT NULL,
    `avatar` VARCHAR(512) DEFAULT NULL,
    `phone` VARCHAR(32) DEFAULT NULL,
    `role` VARCHAR(16) DEFAULT 'user' COMMENT 'user-普通用户/manager-小区管理人/admin-管理员',
    `status` INT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 小区表
CREATE TABLE IF NOT EXISTS `community` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(128) NOT NULL,
    `address` VARCHAR(256) DEFAULT NULL,
    `latitude` DECIMAL(10,6) DEFAULT NULL,
    `longitude` DECIMAL(10,6) DEFAULT NULL,
    `total_parking_spots` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 车位表
CREATE TABLE IF NOT EXISTS `parking_spot` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `community_id` BIGINT NOT NULL,
    `owner_id` BIGINT NOT NULL,
    `spot_number` VARCHAR(32) NOT NULL,
    `status` VARCHAR(32) DEFAULT 'available' COMMENT 'available/occupied/reserved',
    `price_per_hour` DECIMAL(10,2) NOT NULL,
    `description` VARCHAR(512) DEFAULT NULL,
    `images` VARCHAR(4096) DEFAULT NULL COMMENT 'JSON array of image URLs',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_community` (`community_id`),
    KEY `idx_owner` (`owner_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_number` VARCHAR(32) NOT NULL,
    `user_id` BIGINT NOT NULL,
    `parking_spot_id` BIGINT NOT NULL,
    `start_time` DATETIME NOT NULL,
    `end_time` DATETIME NOT NULL,
    `hours` INT NOT NULL,
    `price_per_hour` DECIMAL(10,2) NOT NULL,
    `total_price` DECIMAL(10,2) NOT NULL,
    `status` VARCHAR(32) DEFAULT 'pending' COMMENT 'pending/ongoing/completed/cancelled',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_number` (`order_number`),
    KEY `idx_user` (`user_id`),
    KEY `idx_spot` (`parking_spot_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始小区数据
INSERT INTO `community` (`name`, `address`, `latitude`, `longitude`, `total_parking_spots`) VALUES
('阳光花园', 'XX路 88 号', 31.230416, 121.473701, 100),
('翠湖小区', 'XX大道 18 号', 31.231234, 121.474512, 80);
