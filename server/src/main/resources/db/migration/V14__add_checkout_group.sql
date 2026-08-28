-- 一次结算请求的记录
-- 解决的是“跨店结算”问题。
CREATE TABLE checkout_group
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '结算组主键',
    checkout_no  VARCHAR(40)    NOT NULL COMMENT '一次结算的稳定业务编号',
    consumer_id  BIGINT         NOT NULL COMMENT '消费者 ID',
    status       VARCHAR(24)    NOT NULL COMMENT '结算组状态',
    total_amount DECIMAL(10, 2) NOT NULL COMMENT '结算总金额',
    created_at   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_checkout_group_no (checkout_no),
    INDEX idx_checkout_group_consumer_created (consumer_id, created_at)
);

-- 现有订单先允许为空，兼容历史单店订单
ALTER TABLE commerce_order
    ADD COLUMN checkout_group_id BIGINT NULL
        COMMENT '所属结算组 ID';

ALTER TABLE commerce_order
    ADD INDEX idx_order_checkout_group (checkout_group_id);

-- 保留原有 order_id，新增结算组关联
ALTER TABLE idempotent_request
    ADD COLUMN checkout_group_id BIGINT NULL
        COMMENT '幂等请求对应的结算组 ID';

ALTER TABLE idempotent_request
    ADD INDEX idx_idempotent_checkout_group (checkout_group_id);