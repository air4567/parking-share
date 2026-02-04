-- 适用于腾讯云/微信云托管 MySQL（已去掉易报错的 SET/LOCK，统一 utf8mb4_general_ci）
-- 使用前：在 DMC 里先选中要导入的数据库（或先创建 parking_share 再选中），再执行本脚本

-- 若云库不允许建库可删掉下面两行，并先在控制台建好库再选库执行
CREATE DATABASE IF NOT EXISTS parking_share DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE parking_share;

-- 用户表
DROP TABLE IF EXISTS `order`;
DROP TABLE IF EXISTS `parking_spot`;
DROP TABLE IF EXISTS `community`;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `openid` varchar(64) NOT NULL COMMENT '微信openid',
  `nickname` varchar(64) DEFAULT NULL,
  `avatar` varchar(512) DEFAULT NULL,
  `phone` varchar(32) DEFAULT NULL,
  `role` varchar(16) DEFAULT 'user' COMMENT 'user-普通用户/manager-小区管理人/admin-管理员',
  `status` int DEFAULT 1,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `community` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL,
  `address` varchar(256) DEFAULT NULL,
  `latitude` decimal(10,6) DEFAULT NULL,
  `longitude` decimal(10,6) DEFAULT NULL,
  `total_parking_spots` int DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `parking_spot` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `community_id` bigint NOT NULL,
  `owner_id` bigint NOT NULL,
  `spot_number` varchar(32) NOT NULL,
  `status` varchar(32) DEFAULT 'available' COMMENT 'available/occupied/reserved',
  `price_per_hour` decimal(10,2) NOT NULL,
  `description` varchar(512) DEFAULT NULL,
  `images` varchar(4096) DEFAULT NULL COMMENT 'JSON array of image URLs',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_community` (`community_id`),
  KEY `idx_owner` (`owner_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_number` varchar(32) NOT NULL,
  `user_id` bigint NOT NULL,
  `parking_spot_id` bigint NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `hours` int NOT NULL,
  `price_per_hour` decimal(10,2) NOT NULL,
  `total_price` decimal(10,2) NOT NULL,
  `status` varchar(32) DEFAULT 'pending' COMMENT 'pending/ongoing/completed/cancelled',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_number` (`order_number`),
  KEY `idx_user` (`user_id`),
  KEY `idx_spot` (`parking_spot_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 小区数据（你导出的 3 条）
INSERT INTO `community` (`id`,`name`,`address`,`latitude`,`longitude`,`total_parking_spots`,`create_time`,`update_time`,`deleted`) VALUES
(1,'阳光花园','XX路 88 号',31.230416,121.473701,100,'2026-01-25 12:51:12','2026-01-25 12:51:12',0),
(2,'翠湖小区','XX大道 18 号',31.231234,121.474512,80,'2026-01-25 12:51:12','2026-01-25 12:51:12',0),
(3,'潮流小区','潮阳区柏丽湾(南中路东100米)',23.244589,116.598220,0,'2026-01-29 12:13:12','2026-01-29 12:13:12',0);
