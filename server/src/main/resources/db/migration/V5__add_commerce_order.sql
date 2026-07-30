-- commerce_order：订单主表，记录一笔订单属于哪个商家、哪个消费者、当前状态和总金额。
-- commerce_order_item：订单明细表，记录订单里买了哪些 SKU，并保存商品名和价格快照。
-- 快照的意思是：历史订单不能因为商品后来改名、改价而变化。

-- sku_name_snapshot 是订单项里的“历史快照”。
-- 假设用户今天买了「蓝牙耳机 Pro版」，价格 199.00；明天商家把 SKU 改名成「蓝牙耳机 2026 新款」，价格改成 299.00。
-- 用户再查历史订单时，不能看到订单里的商品名和价格跟着变了，否则账就乱了。所以订单项保存下单那一刻的名称和价格：sku_name_snapshot、sale_price。

-- order_no 是给人和外部系统看的订单号。
-- order_no 是业务订单号，给用户、客服、支付、对账用
-- 因为 id 偏技术，可能不适合暴露给用户；order_no 可以设计成包含日期、渠道、随机数，更适合排查和展示。

CREATE TABLE commerce_order (
                                id BIGINT PRIMARY KEY,
                                order_no VARCHAR(40) NOT NULL,
                                tenant_id BIGINT NOT NULL,
                                consumer_id BIGINT NOT NULL,
                                status VARCHAR(24) NOT NULL,
                                total_amount DECIMAL(10, 2) NOT NULL,
                                expire_at DATETIME NOT NULL,
                                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                UNIQUE KEY uk_order_no (order_no),
                                INDEX idx_tenant_status_created (tenant_id, status, created_at),
                                INDEX idx_consumer_created (consumer_id, created_at)
);

CREATE TABLE commerce_order_item (
                                     id BIGINT PRIMARY KEY,
                                     order_id BIGINT NOT NULL,
                                     sku_id BIGINT NOT NULL,
                                     sku_name_snapshot VARCHAR(128) NOT NULL,
                                     sale_price DECIMAL(10, 2) NOT NULL,
                                     quantity INT NOT NULL,
                                     created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                     INDEX idx_order_id (order_id),
                                     INDEX idx_sku_id (sku_id)
);
