/*
促销补偿记录表。

当 Redis 已经成功预扣抢购资格，但后续异步创建促销订单永久失败时，
系统通过本表记录补偿任务，后续将活动库存和用户限购数量恢复回来。

本表主要记录：
1. 哪一条抢购资格需要补偿；
2. 属于哪个活动商品、租户和消费者；
3. 补偿的库存数量和用户限购数量；
4. 补偿原因及处理状态；
5. 补偿是否已经完成。

通过 reservation_id + compensation_type 唯一约束，
保证同一条抢购资格的同一种补偿不会被重复执行。
*/

CREATE TABLE promotion_compensation_records
(
    id                   BIGINT PRIMARY KEY AUTO_INCREMENT,
    reservation_id       VARCHAR(64)  NOT NULL,
    activity_item_id     BIGINT       NOT NULL,
    tenant_id            BIGINT       NOT NULL,
    consumer_id          BIGINT       NOT NULL,
    compensation_type    VARCHAR(32)  NOT NULL,
    quantity             INT          NOT NULL,
    stock_change         INT          NOT NULL,
    user_quantity_change INT          NOT NULL,
    reason               VARCHAR(255) NOT NULL,
    status               VARCHAR(24)  NOT NULL DEFAULT 'PENDING',
    created_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at         DATETIME     NULL,
    updated_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT chk_compensation_quantity
        CHECK (quantity > 0),

    CONSTRAINT chk_compensation_status
        CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')),

    UNIQUE KEY uk_compensation_reservation_type
        (reservation_id, compensation_type),

    INDEX idx_compensation_status
        (status, created_at),

    INDEX idx_compensation_consumer
        (consumer_id, created_at)
);