-- ============================================
-- 钓迹 App 数据库初始化脚本
-- 数据库名: diaoji_system
-- ============================================

CREATE DATABASE IF NOT EXISTS diaoji_system DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
USE diaoji_system;

-- ============================================
-- 1. 用户表
-- ============================================
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `phone` varchar(11) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '密码（BCrypt加密）',
  `security_question` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '密保问题',
  `security_answer` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '密保答案',
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '昵称',
  `avatar_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '头像URL',
  `fishing_styles` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '钓法偏好（逗号分隔）',
  `guide_status` tinyint DEFAULT '0' COMMENT '引导状态：0-未完成，1-已完成',
  `openid` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '微信openid（可选绑定）',
  `unionid` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '微信unionid（可选绑定）',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标记',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `phone` (`phone`),
  KEY `idx_phone` (`phone`),
  KEY `idx_openid` (`openid`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 钓点表
-- ============================================
DROP TABLE IF EXISTS `t_fishing_spot`;
CREATE TABLE `t_fishing_spot` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(30) NOT NULL COMMENT '钓点名称',
  `description` varchar(200) DEFAULT NULL COMMENT '简介',
  `longitude` decimal(10,6) NOT NULL COMMENT '经度',
  `latitude` decimal(10,6) NOT NULL COMMENT '纬度',
  `geohash` varchar(7) DEFAULT NULL COMMENT 'Geohash前6位（用于附近查询去重）',
  `province` varchar(20) DEFAULT NULL COMMENT '省份',
  `city` varchar(20) DEFAULT NULL COMMENT '城市',
  `district` varchar(20) DEFAULT NULL COMMENT '区县',
  `address` varchar(100) DEFAULT NULL COMMENT '详细地址',
  `fishing_styles` json DEFAULT NULL COMMENT '支持的钓法（JSON数组）',
  `is_free` tinyint DEFAULT '1' COMMENT '是否免费：0=收费，1=免费',
  `price_info` varchar(100) DEFAULT NULL COMMENT '收费标准',
  `avg_rating` decimal(2,1) DEFAULT '0.0' COMMENT '平均评分',
  `total_reviews` int DEFAULT '0' COMMENT '评价总数',
  `total_records` int DEFAULT '0' COMMENT '渔获记录总数',
  `photos` json DEFAULT NULL COMMENT '钓点照片URLs（JSON数组）',
  `created_by` bigint DEFAULT NULL COMMENT '创建者用户ID',
  `status` tinyint DEFAULT '1' COMMENT '状态：0=待审核，1=已上线，2=下架',
  `deleted` tinyint DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_geohash` (`geohash`),
  KEY `idx_status` (`status`),
  KEY `idx_created_by` (`created_by`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='钓点表';

-- ============================================
-- 3. 渔获记录表
-- ============================================
DROP TABLE IF EXISTS `t_fish_record`;
CREATE TABLE `t_fish_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `spot_id` bigint DEFAULT NULL COMMENT '关联钓点ID',
  `spot_name` varchar(30) DEFAULT NULL COMMENT '钓点名称快照',
  `fish_species` varchar(30) NOT NULL COMMENT '鱼种名称',
  `weight` decimal(6,2) NOT NULL COMMENT '重量（斤）',
  `length` decimal(6,1) DEFAULT NULL COMMENT '长度（cm）',
  `quantity` int DEFAULT '1' COMMENT '数量',
  `photo_url` varchar(512) DEFAULT NULL COMMENT '主图URL',
  `photos` json DEFAULT NULL COMMENT '照片URLs（JSON数组，最多3张）',
  `weather_json` json DEFAULT NULL COMMENT '天气快照',
  `gps_longitude` decimal(10,6) DEFAULT NULL COMMENT 'GPS经度',
  `gps_latitude` decimal(10,6) DEFAULT NULL COMMENT 'GPS纬度',
  `fish_feeling` varchar(5) DEFAULT NULL COMMENT '鱼情：好/一般/差',
  `note` varchar(100) DEFAULT NULL COMMENT '备注',
  `poster_url` varchar(512) DEFAULT NULL COMMENT '战绩海报URL',
  `deleted` tinyint DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_spot_id` (`spot_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='渔获记录表';

-- ============================================
-- 4. 动态表（钓友圈）
-- ============================================
DROP TABLE IF EXISTS `t_post`;
CREATE TABLE `t_post` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '发布者ID',
  `content` varchar(500) DEFAULT NULL COMMENT '正文',
  `photos` json DEFAULT NULL COMMENT '图片URLs（JSON数组）',
  `spot_id` bigint DEFAULT NULL COMMENT '关联钓点ID',
  `fish_record_id` bigint DEFAULT NULL COMMENT '关联渔获记录ID',
  `like_count` int DEFAULT '0' COMMENT '点赞数',
  `comment_count` int DEFAULT '0' COMMENT '评论数',
  `share_count` int DEFAULT '0' COMMENT '分享数',
  `status` tinyint DEFAULT '1' COMMENT '状态：0=草稿，1=已发布，2=未通过，3=已删除',
  `deleted` tinyint DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='动态表';

-- ============================================
-- 5. 评论表
-- ============================================
DROP TABLE IF EXISTS `t_comment`;
CREATE TABLE `t_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '动态ID',
  `user_id` bigint NOT NULL COMMENT '评论者ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父评论ID（0=一级评论）',
  `reply_to_user_id` bigint DEFAULT NULL COMMENT '被回复人ID',
  `content` varchar(200) NOT NULL COMMENT '评论内容',
  `deleted` tinyint DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论表';

-- ============================================
-- 6. 点赞表
-- ============================================
DROP TABLE IF EXISTS `t_like`;
CREATE TABLE `t_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `post_id` bigint NOT NULL COMMENT '动态ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_post` (`user_id`,`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='点赞表';

-- ============================================
-- 7. 收藏表
-- ============================================
DROP TABLE IF EXISTS `t_favorite`;
CREATE TABLE `t_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `spot_id` bigint NOT NULL COMMENT '钓点ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_spot` (`user_id`,`spot_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='钓点收藏表';

-- ============================================
-- 8. 关注关系表
-- ============================================
DROP TABLE IF EXISTS `t_follow`;
CREATE TABLE `t_follow` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `follower_id` bigint NOT NULL COMMENT '关注者ID',
  `followee_id` bigint NOT NULL COMMENT '被关注者ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follow` (`follower_id`,`followee_id`),
  KEY `idx_followee` (`followee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='关注关系表';

-- ============================================
-- 9. 钓点评价表
-- ============================================
DROP TABLE IF EXISTS `t_spot_review`;
CREATE TABLE `t_spot_review` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `spot_id` bigint NOT NULL COMMENT '钓点ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `rating` tinyint NOT NULL COMMENT '评分1-5',
  `content` varchar(200) DEFAULT NULL COMMENT '评价内容',
  `photos` json DEFAULT NULL COMMENT '评价照片',
  `deleted` tinyint DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_spot_user` (`spot_id`,`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='钓点评价表';

-- ============================================
-- 10. 鱼种字典表
-- ============================================
DROP TABLE IF EXISTS `t_fish_species`;
CREATE TABLE `t_fish_species` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name_zh` varchar(30) NOT NULL COMMENT '中文名',
  `name_en` varchar(50) DEFAULT NULL COMMENT '英文名',
  `family` varchar(30) DEFAULT NULL COMMENT '科属',
  `fish_type` varchar(10) NOT NULL COMMENT '类型：淡水/海水',
  `icon` varchar(512) DEFAULT NULL COMMENT '图标URL',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name_zh` (`name_zh`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鱼种字典';

-- ============================================
-- 11. 成就定义表
-- ============================================
DROP TABLE IF EXISTS `t_achievement`;
CREATE TABLE `t_achievement` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(30) NOT NULL COMMENT '成就代码',
  `name` varchar(20) NOT NULL COMMENT '成就名称',
  `icon` varchar(10) DEFAULT NULL COMMENT '图标emoji',
  `description` varchar(100) DEFAULT NULL COMMENT '描述',
  `condition_type` varchar(20) DEFAULT NULL COMMENT '条件类型',
  `condition_value` int DEFAULT NULL COMMENT '条件值',
  `reward_desc` varchar(50) DEFAULT NULL COMMENT '奖励描述',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成就定义表';

-- ============================================
-- 12. 用户成就表
-- ============================================
DROP TABLE IF EXISTS `t_user_achievement`;
CREATE TABLE `t_user_achievement` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `achievement_id` bigint NOT NULL COMMENT '成就ID',
  `unlocked_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '解锁时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_achievement` (`user_id`,`achievement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户成就表';

-- ============================================
-- 13. 鱼情指数缓存表
-- ============================================
DROP TABLE IF EXISTS `t_fish_index_cache`;
CREATE TABLE `t_fish_index_cache` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `location_key` varchar(20) NOT NULL COMMENT '位置标识（城市码）',
  `index_date` date NOT NULL COMMENT '指数日期',
  `total_score` decimal(4,2) DEFAULT '0.00' COMMENT '总指数（0-10）',
  `pressure_score` decimal(4,2) DEFAULT '0.00' COMMENT '气压分',
  `water_temp_score` decimal(4,2) DEFAULT '0.00' COMMENT '水温分',
  `moon_score` decimal(4,2) DEFAULT '0.00' COMMENT '月相分',
  `weather_score` decimal(4,2) DEFAULT '0.00' COMMENT '天气分',
  `wind_score` decimal(4,2) DEFAULT '0.00' COMMENT '风速分',
  `raw_data` json DEFAULT NULL COMMENT '原始API数据',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_location_date` (`location_key`,`index_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鱼情指数缓存表';

-- ============================================
-- 初始化测试数据
-- ============================================

-- 插入测试用户（密码都是 123456，BCrypt加密后）
INSERT INTO `t_user` (`phone`, `password`, `security_question`, `security_answer`, `nickname`, `guide_status`) VALUES
('13800000000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '你最喜欢的鱼是什么？', '鲈鱼', '演示用户', 1);

-- 插入常见鱼种
INSERT INTO `t_fish_species` (`name_zh`, `name_en`, `family`, `fish_type`) VALUES
('鲈鱼', 'Bass', '太阳鱼科', '淡水'),
('鳜鱼', 'Mandarin Fish', '鳜科', '淡水'),
('鲈鱼', 'Sea Bass', '锯鳞鲈科', '海水'),
('鲷鱼', 'Snapper', '鲷科', '海水'),
('鳟鱼', 'Trout', '鲑科', '淡水'),
('鲶鱼', 'Catfish', '鲶科', '淡水'),
('鲤鱼', 'Carp', '鲤科', '淡水'),
('草鱼', 'Grass Carp', '鲤科', '淡水'),
('青鱼', 'Black Carp', '鲤科', '淡水'),
('罗非鱼', 'Tilapia', '丽鱼科', '淡水');

-- 插入成就定义
INSERT INTO `t_achievement` (`code`, `name`, `icon`, `description`, `condition_type`, `condition_value`) VALUES
('first_catch', '首战告捷', '🎣', '记录你的第一条渔获', 'fish_record_count', 1),
('catch_10', '小有所成', '🐟', '累计记录10条渔获', 'fish_record_count', 10),
('catch_100', '钓鱼达人', '🐠', '累计记录100条渔获', 'fish_record_count', 100),
('spot_10', '足迹满满', '📍', '添加10个钓点', 'spot_count', 10),
('like_100', '人气钓友', '❤️', '获得100个赞', 'total_likes', 100);
