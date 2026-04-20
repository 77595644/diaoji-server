-- 钓迹数据库用户表结构更新
-- 从微信登录改为手机号密码登录

-- 如果表已存在，先备份后重建
DROP TABLE IF EXISTS t_user;

CREATE TABLE t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    phone VARCHAR(11) NOT NULL UNIQUE COMMENT '手机号',
    password VARCHAR(255) NOT NULL DEFAULT '' COMMENT '密码',
    security_question VARCHAR(100) NOT NULL DEFAULT '' COMMENT '密保问题',
    security_answer VARCHAR(255) NOT NULL DEFAULT '' COMMENT '密保答案',
    nickname VARCHAR(50) DEFAULT '' COMMENT '昵称',
    avatar_url VARCHAR(255) DEFAULT '' COMMENT '头像URL',
    fishing_styles VARCHAR(100) DEFAULT '' COMMENT '钓法偏好',
    guide_status TINYINT DEFAULT 0 COMMENT '引导状态：0-未完成，1-已完成',
    openid VARCHAR(100) DEFAULT '' COMMENT '微信openid（可选绑定）',
    unionid VARCHAR(100) DEFAULT '' COMMENT '微信unionid（可选绑定）',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_phone (phone),
    INDEX idx_openid (openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入演示用户
INSERT INTO t_user (phone, password, nickname, security_question, security_answer, guide_status)
VALUES ('13800000000', '123456', '演示用户', '您最喜欢的钓鱼地点是？', '珠江', 1);
