/*
tenant_id：所属商家租户。
所有活动、活动商品和抢购资格都必须属于同一个租户，防止商家数据串用。

start_at / end_at：活动开始和结束时间。
时间统一由服务端校验，数据库保证开始时间早于结束时间。

status：活动生命周期状态。
DRAFT 草稿、SCHEDULED 已排期、ACTIVE 进行中、
ENDED 已结束、CANCELLED 已取消。
数据库只限制状态值是否合法，状态转换顺序由 Service 控制。
*/

-- 限量促销活动主表
CREATE TABLE promotion_activities
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id  BIGINT       NOT NULL,
    name       VARCHAR(128) NOT NULL,
    start_at   DATETIME     NOT NULL,
    end_at     DATETIME     NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT chk_promotion_activity_time
        CHECK (start_at < end_at),

    -- 这表示数据库只允许这 5 种状态，避免出现拼写错误或非法状态。
    CONSTRAINT chk_promotion_activity_status
        CHECK (status IN (
                          'DRAFT',
                          'SCHEDULED',
                          'ACTIVE',
                          'ENDED',
                          'CANCELLED'
            )),

    INDEX idx_promotion_tenant_status (tenant_id, status),
    INDEX idx_promotion_time (status, start_at, end_at)
);


/*
activity_price：活动价格快照。
不能实时读取 product_sku.sale_price，因为普通商品价格之后可能被修改。

stock_total：创建活动时从普通可售库存划拨出的活动总库存。
stock_available：当前还可以被抢购的活动库存。
创建活动时两者相等，抢购成功后只减少 stock_available。

limit_per_user：单个消费者对该活动商品的最大购买数量。
不能只依赖前端按钮限制，后端必须再次校验。
*/
-- 限量促销活动商品表
CREATE TABLE promotion_items
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id    BIGINT       NOT NULL,
    tenant_id      BIGINT       NOT NULL,
    sku_id         BIGINT       NOT NULL,
    activity_price DECIMAL(10,2) NOT NULL,
    stock_total     INT NOT NULL,
    stock_available INT NOT NULL,
    limit_per_user INT          NOT NULL DEFAULT 1,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT chk_promotion_item_price
        CHECK (activity_price > 0),

    CONSTRAINT chk_promotion_item_stock_total
        CHECK (stock_total > 0),

    CONSTRAINT chk_promotion_item_stock_available
        CHECK (stock_available >= 0
            AND stock_available <= stock_total
            ),

    CONSTRAINT chk_promotion_item_limit
        CHECK (limit_per_user > 0),

    UNIQUE KEY uk_promotion_activity_sku (activity_id, sku_id),
    INDEX idx_promotion_item_tenant (tenant_id),
    INDEX idx_promotion_item_sku (sku_id)
);


/*
reservation_id：一次抢购资格的稳定业务编号。
后续异步创建订单、查询结果和补偿流程都使用它。

request_key：抢购请求幂等键。
防止双击、网络重试或重复消息产生多条抢购资格。

unit_price_snapshot：获得抢购资格时保存的活动价格快照。
后续商品或活动价格变化不能影响本次资格对应的价格。

status：抢购资格处理状态。
PENDING_ORDER：等待创建订单；
ORDER_CREATED：订单已创建；
FAILED：创建订单失败；
COMPENSATED：失败后的库存或资格补偿已完成。
*/

-- 限量促销购买资格表
CREATE TABLE promotion_reservations
(
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    reservation_id      VARCHAR(64)  NOT NULL,
    activity_id         BIGINT       NOT NULL,
    activity_item_id    BIGINT       NOT NULL,
    tenant_id           BIGINT       NOT NULL,
    consumer_id         BIGINT       NOT NULL,
    request_key         VARCHAR(100) NOT NULL,
    quantity            INT          NOT NULL,
    unit_price_snapshot DECIMAL(10,2) NOT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'PENDING_ORDER',
    order_id            BIGINT       NULL,
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT chk_promotion_reservation_status
        CHECK (status IN (
                          'PENDING_ORDER',
                          'ORDER_CREATED',
                          'FAILED',
                          'COMPENSATED'
            )),

    CONSTRAINT chk_promotion_reservation_quantity
        CHECK (quantity > 0),

    CONSTRAINT chk_promotion_reservation_price
        CHECK (unit_price_snapshot > 0),

    UNIQUE KEY uk_promotion_reservation_order (order_id),
    UNIQUE KEY uk_promotion_reservation_id (reservation_id),
    UNIQUE KEY uk_promotion_reservation_request
        (consumer_id, activity_item_id, request_key),
    INDEX idx_promotion_reservation_item_user
        (activity_item_id, consumer_id, status),
    INDEX idx_promotion_reservation_activity_status
        (activity_id, status),
    INDEX idx_promotion_reservation_order
        (order_id)
);