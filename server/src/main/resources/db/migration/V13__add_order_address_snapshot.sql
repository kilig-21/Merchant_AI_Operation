-- 订单收货地址快照表
-- 保存订单创建时的地址内容。
-- 地址主数据后续发生修改或删除时，历史订单仍保持原样。
CREATE TABLE commerce_order_address
(
    id                BIGINT       PRIMARY KEY      AUTO_INCREMENT COMMENT '地址快照主键',

    order_id          BIGINT       NOT NULL COMMENT '订单 ID，对应 commerce_order.id',

    consumer_id       BIGINT       NOT NULL COMMENT '消费者用户 ID，便于审计和消费者订单查询',

    source_address_id BIGINT       NULL     COMMENT '创建订单时使用的地址 ID，仅记录来源，不依赖其后续存在',

    receiver_name     VARCHAR(64)  NOT NULL COMMENT '订单创建时的收货人姓名快照',

    receiver_phone    VARCHAR(32)  NOT NULL COMMENT '订单创建时的收货人手机号快照',

    province          VARCHAR(64)  NOT NULL COMMENT '省/自治区/直辖市快照',

    city              VARCHAR(64)  NOT NULL COMMENT '城市快照',

    district          VARCHAR(64)  NOT NULL COMMENT '区/县快照',

    detail_address    VARCHAR(255) NOT NULL COMMENT '详细收货地址快照',

    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '快照创建时间',

    UNIQUE KEY uk_order_address_order_id (order_id),
    INDEX idx_order_address_consumer (consumer_id, created_at)
);