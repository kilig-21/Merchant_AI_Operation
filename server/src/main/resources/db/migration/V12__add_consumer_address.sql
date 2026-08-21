-- 消费者收货地址表
-- 一条记录代表消费者保存的一组收货信息。
-- 地址属于消费者，不属于商家租户。
CREATE TABLE consumer_address
(
    id             BIGINT       PRIMARY KEY     AUTO_INCREMENT COMMENT '地址主键',

    consumer_id    BIGINT       NOT NULL COMMENT '消费者用户 ID，对应 sys_user.id',

    receiver_name  VARCHAR(64)  NOT NULL COMMENT '收货人姓名',

    receiver_phone VARCHAR(32)  NOT NULL COMMENT '收货人手机号',

    province       VARCHAR(64)  NOT NULL COMMENT '省/自治区/直辖市',

    city           VARCHAR(64)  NOT NULL COMMENT '城市',

    district       VARCHAR(64)  NOT NULL COMMENT '区/县',

    detail_address VARCHAR(255) NOT NULL COMMENT '详细街道、楼栋和门牌地址',

    is_default     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否默认地址：0 否，1 是；默认地址约束由服务层保证',

    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '地址创建时间',

    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        COMMENT '地址最后修改时间',

    -- 支持查询某个消费者的默认地址
    INDEX idx_address_consumer_default (consumer_id, is_default),

    -- 支持查询某个消费者的地址列表
    INDEX idx_address_consumer_created (consumer_id, created_at)
);