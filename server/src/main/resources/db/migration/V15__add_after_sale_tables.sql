-- 售后申请主表
CREATE TABLE after_sale_request
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '售后申请 ID',
    request_no       VARCHAR(40)    NOT NULL COMMENT '售后申请业务编号',

    order_id         BIGINT         NOT NULL COMMENT '订单 ID',
    order_item_id    BIGINT         NOT NULL COMMENT '订单项 ID',

    tenant_id        BIGINT         NOT NULL COMMENT '所属商家租户',
    consumer_id      BIGINT         NOT NULL COMMENT '申请消费者',

    quantity         INT            NOT NULL COMMENT '申请售后数量',
    requested_amount DECIMAL(10, 2) NOT NULL COMMENT '申请金额',
    reason           VARCHAR(255)   NOT NULL COMMENT '申请原因',

    status           VARCHAR(24)    NOT NULL COMMENT 'SUBMITTED/REVIEWING/APPROVED/REJECTED',

    merchant_remark  VARCHAR(500)   NULL COMMENT '商家审核备注',
    decided_by       BIGINT         NULL COMMENT '审核商家用户 ID',
    decided_at       DATETIME       NULL COMMENT '审核时间',

    created_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_after_sale_request_no (request_no),
    INDEX idx_after_sale_consumer_created (consumer_id, created_at),
    INDEX idx_after_sale_tenant_status_created (tenant_id, status, created_at),
    INDEX idx_after_sale_order_item (order_id, order_item_id)
);

-- 售后状态变更审计表
CREATE TABLE after_sale_status_log
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '状态日志 ID',
    after_sale_id BIGINT       NOT NULL COMMENT '售后申请 ID',

    from_status   VARCHAR(24)  NULL COMMENT '原状态',
    to_status     VARCHAR(24)  NOT NULL COMMENT '新状态',

    operator_id   BIGINT       NOT NULL COMMENT '操作人 ID',
    operator_type VARCHAR(24)  NOT NULL COMMENT 'CONSUMER/MERCHANT',
    remark        VARCHAR(500) NULL COMMENT '操作备注',

    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_after_sale_log_request_created (after_sale_id, created_at),
    INDEX idx_after_sale_log_operator_created (operator_id, created_at)
);