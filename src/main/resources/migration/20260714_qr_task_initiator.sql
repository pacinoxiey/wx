-- Preserve the submitting user while the QR task is processed asynchronously.
ALTER TABLE wechat_qr_task
  ADD COLUMN initiator_id VARCHAR(64) NULL COMMENT '提交任务的用户 openid';
