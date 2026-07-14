-- Run once on an existing schema shared with pdd_helper.
ALTER TABLE group_buy ADD COLUMN input_type VARCHAR(16) NOT NULL DEFAULT 'TOKEN';

CREATE TABLE wechat_qr_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  group_buy_id BIGINT NULL COMMENT 'pdd_helper creates group_buy and writes this on success',
  qr_url VARCHAR(2048) NOT NULL,
  initiator_id VARCHAR(64) NULL COMMENT '提交任务的用户 openid',
  qr_status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  qr_attempts INT NOT NULL DEFAULT 0,
  qr_error VARCHAR(1000),
  qr_processed_at DATETIME,
  qr_next_attempt_at DATETIME,
  qr_raw_text TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_qr_claim (qr_status, qr_next_attempt_at, id),
  INDEX idx_qr_group_buy (group_buy_id)
);

-- For databases where wechat_qr_task was created by an earlier migration:
-- ALTER TABLE wechat_qr_task MODIFY COLUMN group_buy_id BIGINT NULL;
