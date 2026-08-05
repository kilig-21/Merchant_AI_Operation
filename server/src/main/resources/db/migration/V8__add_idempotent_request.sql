-- 给“下单请求”做身份证:
-- 用户因为双击、网络重试、前端超时，又发了一次同样请求时，后端能判断：这是同一个下单意图，不应该再新建第二个订单。

CREATE TABLE idempotent_request
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    consumer_id   BIGINT       NOT NULL,
    request_key   VARCHAR(100) NOT NULL, -- 真正新的下单意图生成新 key；同一次下单的重试沿用旧 key
    request_hash  VARCHAR(64)  NOT NULL, -- 请求参数指纹；同 key 不同参数时返回 409
    status        VARCHAR(30)  NOT NULL, -- PROCESSING/SUCCESS/FAILED，表示这次请求处理到哪一步
    order_id      BIGINT       NULL,
    response_body TEXT         NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_idempotent_consumer_key (consumer_id, request_key),
    KEY idx_idempotent_order_id (order_id)
);