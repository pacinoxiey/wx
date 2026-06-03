CREATE TABLE `Counters` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `count` int(11) NOT NULL DEFAULT '1',
  `createdAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatedAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;


-- 拼团信息表
-- 状态不在DB中维护，通过 expire_time 与当前时间比较动态判定:
--   expire_time > NOW()  → 进行中
--   expire_time <= NOW() → 已过期
CREATE TABLE `group_buy` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT 'ID',
  `raw_text`        TEXT         NOT NULL                 COMMENT '用户粘贴的原始文本',
  `platform`        VARCHAR(32)  NOT NULL DEFAULT '拼多多' COMMENT '来源平台',
  `product_name`    VARCHAR(512) NOT NULL                 COMMENT '商品名称',
  `product_desc`    VARCHAR(512)                          COMMENT '商品描述(【...】及后面的描述文字)',
  `group_price`     DECIMAL(10,2)                         COMMENT '拼团价格',
  `remaining_slots` INT          NOT NULL DEFAULT 1       COMMENT '剩余名额',
  `share_code`      VARCHAR(128)                          COMMENT '口令码',
  `share_url`       VARCHAR(512)                          COMMENT '原始链接(如有)',
  `initiator_id`    VARCHAR(64)  NOT NULL                 COMMENT '发起人用户 openid',
  `expire_time`     DATETIME     NOT NULL                 COMMENT '过期时间(创建时间+24h)',
  `created_at`      DATETIME     NOT NULL DEFAULT NOW()   COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_initiator` (`initiator_id`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼团信息表';


-- 用户关注关键词表
CREATE TABLE `user_keyword` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT  COMMENT 'ID',
  `openid`     VARCHAR(64)  NOT NULL                 COMMENT '用户 openid',
  `keyword`    VARCHAR(256) NOT NULL                 COMMENT '关注的关键词',
  `created_at` DATETIME     NOT NULL DEFAULT NOW()   COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_openid` (`openid`),
  UNIQUE KEY `idx_openid_keyword` (`openid`, `keyword`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注关键词表';