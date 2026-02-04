-- 若已存在数据库，请执行本文件以扩大车位表 images 列，避免带多图发布时报 500
-- 执行方式示例: mysql -u root -p your_database < migration_parking_spot_images.sql
ALTER TABLE `parking_spot` MODIFY COLUMN `images` VARCHAR(4096) DEFAULT NULL COMMENT 'JSON array of image URLs';
