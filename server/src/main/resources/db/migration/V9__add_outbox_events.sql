/*
创建订单 + 写入 Outbox 事件
必须在同一个 MySQL 事务中完成
        ↓
后台发布任务再把事件发送到 RabbitMQ
        ↓
RabbitMQ 暂停时，事件仍保存在数据库中，之后可以重试
*/
CREATE TABLE outbox_events
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id       VARCHAR(64)  NOT NULL,
    aggregate_type VARCHAR(64)  NOT NULL,
    aggregate_id   BIGINT       NOT NULL,
    event_type     VARCHAR(64)  NOT NULL,
    payload        JSON         NOT NULL,
    status         VARCHAR(24)  NOT NULL DEFAULT 'PENDING',
    retry_count    INT          NOT NULL DEFAULT 0,
    next_retry_at  DATETIME     NULL,
    published_at   DATETIME     NULL,
    last_error     VARCHAR(500) NULL,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_outbox_event_id (event_id),
    INDEX idx_outbox_status_retry (status, next_retry_at, id),
    INDEX idx_outbox_aggregate (aggregate_type, aggregate_id)
);