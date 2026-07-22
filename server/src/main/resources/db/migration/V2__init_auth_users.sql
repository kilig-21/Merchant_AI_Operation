INSERT INTO tenant (id, name, status)
VALUES
    (1001, 'kilig数码旗舰店', 1),
    (1002, 'kilig服饰旗舰店', 1);

INSERT INTO sys_user (id, tenant_id, username, password_hash, user_type, status)
VALUES
    -- 消费者账号，后面测普通用户登录
    (1, NULL, 'consumer_001', '$2a$10$BIR1qkZ5WF8E6vp9iwbPaOUpNvT7j6IiehLDyR1Bp/95.LtRixRiO', 'CONSUMER', 1),

    -- 商家 A 管理员，tenant_id = 1001
    (2, 1001, 'merchant_a_admin', '$2a$10$BIR1qkZ5WF8E6vp9iwbPaOUpNvT7j6IiehLDyR1Bp/95.LtRixRiO', 'MERCHANT_ADMIN', 1),


    -- 商家 B 管理员，tenant_id = 1002。
    (3, 1002, 'merchant_b_admin', '$2a$10$BIR1qkZ5WF8E6vp9iwbPaOUpNvT7j6IiehLDyR1Bp/95.LtRixRiO', 'MERCHANT_ADMIN', 1);

