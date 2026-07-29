-- consumer_id：谁的购物车。购物车属于消费者，不属于商家。
-- sku_id：买的是哪个具体规格，比如“白色 / 标准版”。
-- quantity：数量，后面接口里会限制必须大于等于 1。
-- uk_consumer_sku：重点。它保证同一个消费者的购物车里，同一个 SKU 只能有一条记录。重复加入时，我们后端就做“数量 + 新增数量”。

CREATE TABLE cart_item(
    id BIGINT PRIMARY KEY,
    consumer_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_consumer_sku (consumer_id, sku_id),
    INDEX idx_consumer_id (consumer_id),
    INDEX idx_sku_id (sku_id)
);