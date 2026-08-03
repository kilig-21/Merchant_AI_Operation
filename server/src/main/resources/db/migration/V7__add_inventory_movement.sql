

-- business_type：这次库存为什么变，比如下单锁库、支付扣锁定库存。
-- business_no：是哪一笔业务导致的，比如订单号。
-- available_change / locked_change：库存变了多少，正数是增加，负数是减少。



CREATE TABLE inventory_movement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    business_type VARCHAR(40) NOT NULL COMMENT '库存变化类型，例如 ORDER_LOCK, ORDER_PAID',
    business_no VARCHAR(64) NOT NULL COMMENT '业务单号，例如订单号',
    available_change INT NOT NULL DEFAULT 0 COMMENT '可售库存变化量，减少为负数',
    locked_change INT NOT NULL DEFAULT 0 COMMENT '锁定库存变化量，减少为负数',
    available_after INT NOT NULL COMMENT '变化后的可售库存',
    locked_after INT NOT NULL COMMENT '变化后的锁定库存',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_inventory_business (business_type, business_no, sku_id),
    INDEX idx_inventory_sku_time (sku_id, created_at),
    INDEX idx_inventory_tenant_time (tenant_id, created_at)
);