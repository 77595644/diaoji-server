-- =============================================
-- 钓迹 App — 数据库初始化脚本
-- 数据库：diaoji_system
-- =============================================

CREATE DATABASE IF NOT EXISTS diaoji_system
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE diaoji_system;

-- -------------------------------------------------
-- 1. t_user 用户表
-- -------------------------------------------------
CREATE TABLE IF NOT EXISTS t_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    openid      VARCHAR(64) NOT NULL UNIQUE COMMENT '微信OpenID',
    unionid     VARCHAR(64) COMMENT '微信UnionID',
    nickname    VARCHAR(20) COMMENT '昵称',
    avatar_url  VARCHAR(512) COMMENT '头像URL',
    fishing_styles  JSON COMMENT '钓法偏好，JSON数组',
    guide_status   TINYINT DEFAULT 0 COMMENT '引导状态：0=未完成，1=已完成',
    deleted     TINYINT DEFAULT 0 COMMENT '逻辑删除：0=未删，1=已删',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_openid (openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- -------------------------------------------------
-- 2. t_fish_species 鱼种字典
-- -------------------------------------------------
CREATE TABLE IF NOT EXISTS t_fish_species (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name_zh     VARCHAR(30) NOT NULL COMMENT '中文名',
    name_en     VARCHAR(50) COMMENT '英文名',
    family      VARCHAR(30) COMMENT '科属',
    fish_type   VARCHAR(10) NOT NULL COMMENT '类型：淡水/海水',
    icon        VARCHAR(512) COMMENT '图标URL',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_name_zh (name_zh)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鱼种字典';

-- -------------------------------------------------
-- 3. t_fishing_spot 钓点表
-- -------------------------------------------------
CREATE TABLE IF NOT EXISTS t_fishing_spot (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(30) NOT NULL COMMENT '钓点名称',
    description     VARCHAR(200) COMMENT '简介',
    longitude       DECIMAL(10, 6) NOT NULL COMMENT '经度',
    latitude        DECIMAL(10, 6) NOT NULL COMMENT '纬度',
    geohash         VARCHAR(7) COMMENT 'Geohash前6位',
    province        VARCHAR(20) COMMENT '省份',
    city            VARCHAR(20) COMMENT '城市',
    district        VARCHAR(20) COMMENT '区县',
    address         VARCHAR(100) COMMENT '详细地址',
    fishing_styles  JSON COMMENT '支持的钓法',
    is_free         TINYINT DEFAULT 1 COMMENT '是否免费：0=收费，1=免费',
    price_info      VARCHAR(100) COMMENT '收费标准',
    avg_rating      DECIMAL(2, 1) DEFAULT 0 COMMENT '平均评分',
    total_reviews   INT DEFAULT 0 COMMENT '评价总数',
    total_records   INT DEFAULT 0 COMMENT '渔获记录总数',
    photos          JSON COMMENT '照片URLs',
    created_by      BIGINT COMMENT '创建者用户ID',
    status          TINYINT DEFAULT 1 COMMENT '状态：0=待审核，1=已上线，2=下架',
    deleted         TINYINT DEFAULT 0,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_geohash (geohash),
    INDEX idx_status (status),
    INDEX idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钓点表';

-- -------------------------------------------------
-- 4. t_spot_review 钓点评价
-- -------------------------------------------------
CREATE TABLE IF NOT EXISTS t_spot_review (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    spot_id     BIGINT NOT NULL COMMENT '钓点ID',
    user_id     BIGINT NOT NULL COMMENT '用户ID',
    rating      TINYINT NOT NULL COMMENT '评分1-5',
    content     VARCHAR(200) COMMENT '评价内容',
    photos      JSON COMMENT '评价照片',
    deleted     TINYINT DEFAULT 0,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_spot_user (spot_id, user_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钓点评价表';

-- -------------------------------------------------
-- 5. t_fish_record 渔获记录
-- -------------------------------------------------
CREATE TABLE IF NOT EXISTS t_fish_record (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    spot_id         BIGINT COMMENT '关联钓点ID',
    spot_name       VARCHAR(30) COMMENT '钓点名称快照',
    fish_species    VARCHAR(30) NOT NULL COMMENT '鱼种名称',
    weight          DECIMAL(6, 2) NOT NULL COMMENT '重量（斤）',
    length          DECIMAL(6, 1) COMMENT '长度（cm）',
    quantity        INT DEFAULT 1 COMMENT '数量',
    photo_url       VARCHAR(512) COMMENT '主图URL',
    photos          JSON COMMENT '照片URLs',
    weather_json    JSON COMMENT '天气快照',
    gps_longitude   DECIMAL(10, 6) COMMENT 'GPS经度',
    gps_latitude    DECIMAL(10, 6) COMMENT 'GPS纬度',
    fish_feeling    VARCHAR(5) COMMENT '鱼情：好/一般/差',
    note            VARCHAR(100) COMMENT '备注',
    poster_url      VARCHAR(512) COMMENT '战绩海报URL',
    deleted         TINYINT DEFAULT 0,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_spot_id (spot_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='渔获记录表';

-- -------------------------------------------------
-- 6. t_post 动态/Feed
-- -------------------------------------------------
CREATE TABLE IF NOT EXISTS t_post (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL COMMENT '发布者ID',
    content         VARCHAR(500) COMMENT '正文',
    photos          JSON COMMENT '图片URLs',
    spot_id         BIGINT COMMENT '关联钓点ID',
    fish_record_id  BIGINT COMMENT '关联渔获记录ID',
    like_count      INT DEFAULT 0 COMMENT '点赞数',
    comment_count  INT DEFAULT 0 COMMENT '评论数',
    share_count     INT DEFAULT 0 COMMENT '分享数',
    status          TINYINT DEFAULT 1 COMMENT '状态：0=草稿，1=已发布，2=未通过，3=已删除',
    deleted         TINYINT DEFAULT 0,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态表';

-- -------------------------------------------------
-- 7. t_comment 评论
-- -------------------------------------------------
CREATE TABLE IF NOT EXISTS t_comment (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id     BIGINT NOT NULL COMMENT '动态ID',
    user_id     BIGINT NOT NULL COMMENT '评论者ID',
    parent_id   BIGINT DEFAULT 0 COMMENT '父评论ID（0=一级评论）',
    content     VARCHAR(200) NOT NULL COMMENT '评论内容',
    deleted     TINYINT DEFAULT 0,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- -------------------------------------------------
-- 8. t_like 点赞
-- -------------------------------------------------
CREATE TABLE IF NOT EXISTS t_like (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    post_id     BIGINT NOT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_post (user_id, post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';

-- -------------------------------------------------
-- 9. t_follow 关注关系
-- -------------------------------------------------
CREATE TABLE IF NOT EXISTS t_follow (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    follower_id BIGINT NOT NULL COMMENT '关注者ID',
    followee_id BIGINT NOT NULL COMMENT '被关注者ID',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_follow (follower_id, followee_id),
    INDEX idx_followee (followee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注关系表';

-- -------------------------------------------------
-- 10. t_achievement 成就定义
-- -------------------------------------------------
CREATE TABLE IF NOT EXISTS t_achievement (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(30) NOT NULL UNIQUE COMMENT '成就代码',
    name        VARCHAR(20) NOT NULL COMMENT '成就名称',
    icon        VARCHAR(10) COMMENT '图标emoji',
    description VARCHAR(100) COMMENT '描述',
    condition_type   VARCHAR(20) COMMENT '条件类型',
    condition_value INT COMMENT '条件值',
    reward_desc VARCHAR(50) COMMENT '奖励描述',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成就定义表';

-- -------------------------------------------------
-- 11. t_user_achievement 用户成就
-- -------------------------------------------------
CREATE TABLE IF NOT EXISTS t_user_achievement (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    achievement_id  BIGINT NOT NULL,
    unlocked_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_achievement (user_id, achievement_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户成就表';

-- -------------------------------------------------
-- 12. t_fish_index_cache 鱼情指数缓存
-- -------------------------------------------------
CREATE TABLE IF NOT EXISTS t_fish_index_cache (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    location_key    VARCHAR(20) NOT NULL COMMENT '位置标识（城市码）',
    index_date      DATE NOT NULL COMMENT '指数日期',
    total_score     DECIMAL(4, 2) DEFAULT 0 COMMENT '总指数（0-10）',
    pressure_score  DECIMAL(4, 2) DEFAULT 0 COMMENT '气压分',
    water_temp_score DECIMAL(4, 2) DEFAULT 0 COMMENT '水温分',
    moon_score      DECIMAL(4, 2) DEFAULT 0 COMMENT '月相分',
    weather_score   DECIMAL(4, 2) DEFAULT 0 COMMENT '天气分',
    wind_score      DECIMAL(4, 2) DEFAULT 0 COMMENT '风速分',
    raw_data        JSON COMMENT '原始API数据',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_location_date (location_key, index_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鱼情指数缓存表';

-- -------------------------------------------------
-- 13. t_favorite 收藏（钓点收藏）
-- -------------------------------------------------
CREATE TABLE IF NOT EXISTS t_favorite (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    spot_id     BIGINT NOT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_spot (user_id, spot_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钓点收藏表';

-- -------------------------------------------------
-- 种子数据：鱼种字典（常见淡水鱼）
-- -------------------------------------------------
INSERT INTO t_fish_species (name_zh, name_en, family, fish_type) VALUES
('鲫鱼',    'Crucian Carp',         '鲤科', '淡水'),
('鲤鱼',    'Common Carp',          '鲤科', '淡水'),
('草鱼',    'Grass Carp',           '鲤科', '淡水'),
('青鱼',    'Black Carp',           '鲤科', '淡水'),
('鲢鱼',    'Silver Carp',          '鲤科', '淡水'),
('鳙鱼',    'Bighead Carp',         '鲤科', '淡水'),
('鳜鱼',    'Mandarin Fish',        '鮨科', '淡水'),
('翘嘴鲌',  'Topmouth Gudgeon',     '鲤科', '淡水'),
('马口鱼',  'Japanese Mahseer',     '鲤科', '淡水'),
('黑鱼',    'Snakehead',            '鳢科', '淡水'),
('黄颡鱼',  'Yellow Catfish',       '鲿科', '淡水'),
('鳊鱼',    'Steamed Bream',        '鲤科', '淡水'),
('罗非鱼',  'Nile Tilapia',         '丽鱼科', '淡水'),
('鳗鱼',    'Eel',                  '鳗鲡科', '淡水'),
('鲈鱼',    'Sea Bass',             '狼鲈科', '淡水'),
('太阳鱼',  'Sunfish',              '棘臀鱼科', '淡水'),
('鲶鱼',    'Catfish',              '鲶科', '淡水'),
('泥鳅',    'Loach',                '鳅科', '淡水'),
('河鲀',    'Pufferfish',           '鲀科', '淡水'),
('中华鲟',  'Chinese Sturgeon',     '鲟科', '淡水');

-- -------------------------------------------------
-- 种子数据：成就定义
-- -------------------------------------------------
INSERT INTO t_achievement (code, name, icon, description, condition_type, condition_value, reward_desc) VALUES
('HUNDRED_FISH', '百鱼斩', '🐟', '累计渔获100条鱼', 'fish_count', 100, '称号+头像框'),
('NO_AIR_FISHER', '空军终结者', '😤', '连续10次出钓都有渔获', 'streak_no_air', 10, '称号+200积分'),
('NIGHT_ANGEL', '夜钓达人', '🌙', '累计夜钓记录10次', 'night_count', 10, '称号+头像框'),
('BIG_CATCH', '爆护传说', '💥', '单次渔获重量超过10斤', 'single_weight', 10, '解锁高级海报模板'),
('EXPLORER', '探路者', '🗺️', '上报钓点被审核通过3个', 'spot_count', 3, '称号+300积分'),
('SOCIAL_BUTTERFLY', '切线之王', '🔥', '累计发布动态50条', 'post_count', 50, '称号+头像框');

SELECT '✅ 数据库初始化完成，共创建13张表' AS result;
