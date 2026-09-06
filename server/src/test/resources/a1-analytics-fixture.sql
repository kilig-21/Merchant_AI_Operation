-- A1 fixed analytics fixture. Test-only; never loaded by Flyway.
-- IDs use the 9200000000000 range to avoid collisions with existing tests.

DELETE FROM after_sale_request WHERE id BETWEEN 9200000000000 AND 9200000000999;
DELETE FROM promotion_reservations WHERE id BETWEEN 9200000000000 AND 9200000000999;
DELETE FROM promotion_items WHERE id BETWEEN 9200000000000 AND 9200000000999;
DELETE FROM promotion_activities WHERE id BETWEEN 9200000000000 AND 9200000000999;
DELETE FROM commerce_order_item WHERE id BETWEEN 9200000000000 AND 9200000000999;
DELETE FROM commerce_order WHERE id BETWEEN 9200000000000 AND 9200000000999;
DELETE FROM product_sku WHERE id BETWEEN 9200000000000 AND 9200000000999;
DELETE FROM product_spu WHERE id BETWEEN 9200000000000 AND 9200000000999;
DELETE FROM tenant WHERE id BETWEEN 9200000000000 AND 9200000000999;

INSERT INTO tenant (id, name, status) VALUES
    (9200000000001, 'A1 analytics tenant A', 1),
    (9200000000002, 'A1 analytics tenant B', 1);

INSERT INTO product_spu (id, tenant_id, name, status) VALUES
    (9200000000101, 9200000000001, 'A1 test headphones', 'ON_SALE'),
    (9200000000102, 9200000000001, 'A1 test keyboard', 'ON_SALE'),
    (9200000000103, 9200000000002, 'A1 tenant B product', 'ON_SALE');

INSERT INTO product_sku
    (id, tenant_id, spu_id, sku_name, sale_price, available_stock, locked_stock, version, status)
VALUES
    (9200000000201, 9200000000001, 9200000000101, 'A1 headphones standard', 50.00, 4, 0, 0, 'ON_SALE'),
    (9200000000202, 9200000000001, 9200000000102, 'A1 keyboard standard', 100.00, 20, 0, 0, 'ON_SALE'),
    (9200000000203, 9200000000002, 9200000000103, 'A1 tenant B expensive SKU', 999.00, 2, 0, 0, 'ON_SALE');

INSERT INTO commerce_order
    (id, order_no, tenant_id, consumer_id, status, total_amount, expire_at, created_at, updated_at)
VALUES
    (9200000000301, 'A1-A-PAID-001', 9200000000001, 9200000000901, 'PAID', 200.00,
     '2026-08-01 12:00:00', '2026-08-01 09:00:00', '2026-08-01 09:00:00'),
    (9200000000302, 'A1-A-PAID-002', 9200000000001, 9200000000901, 'PAID', 150.00,
     '2026-08-02 12:00:00', '2026-08-02 10:00:00', '2026-08-02 10:00:00'),
    (9200000000303, 'A1-A-PENDING-001', 9200000000001, 9200000000901, 'PENDING_PAYMENT', 80.00,
     '2026-08-03 12:00:00', '2026-08-03 11:00:00', '2026-08-03 11:00:00'),
    (9200000000304, 'A1-A-CANCELLED-001', 9200000000001, 9200000000901, 'CANCELLED', 40.00,
     '2026-08-02 13:00:00', '2026-08-02 12:00:00', '2026-08-02 12:00:00'),
    (9200000000305, 'A1-A-CLOSED-001', 9200000000001, 9200000000901, 'CLOSED', 60.00,
     '2026-08-03 15:00:00', '2026-08-03 14:00:00', '2026-08-03 14:00:00'),
    (9200000000306, 'A1-B-PAID-001', 9200000000002, 9200000000902, 'PAID', 999.00,
     '2026-08-02 16:00:00', '2026-08-02 15:00:00', '2026-08-02 15:00:00');

INSERT INTO commerce_order_item
    (id, order_id, sku_id, sku_name_snapshot, sale_price, quantity, created_at)
VALUES
    (9200000000401, 9200000000301, 9200000000201, 'A1 headphones standard', 50.00, 2, '2026-08-01 09:00:00'),
    (9200000000402, 9200000000301, 9200000000202, 'A1 keyboard standard', 100.00, 1, '2026-08-01 09:00:00'),
    (9200000000403, 9200000000302, 9200000000201, 'A1 headphones standard', 50.00, 3, '2026-08-02 10:00:00'),
    (9200000000404, 9200000000306, 9200000000203, 'A1 tenant B expensive SKU', 999.00, 1, '2026-08-02 15:00:00');

INSERT INTO promotion_activities
    (id, tenant_id, name, start_at, end_at, status, created_at, updated_at)
VALUES
    (9200000000501, 9200000000001, 'A1 tenant A promotion', '2026-08-01 00:00:00', '2026-08-05 00:00:00', 'ENDED',
     '2026-07-31 10:00:00', '2026-08-05 00:00:00'),
    (9200000000502, 9200000000002, 'A1 tenant B promotion', '2026-08-01 00:00:00', '2026-08-05 00:00:00', 'ENDED',
     '2026-07-31 10:00:00', '2026-08-05 00:00:00');

INSERT INTO promotion_items
    (id, activity_id, tenant_id, sku_id, activity_price, stock_total, stock_available, limit_per_user, created_at, updated_at)
VALUES
    (9200000000551, 9200000000501, 9200000000001, 9200000000201, 30.00, 20, 17, 3,
     '2026-07-31 10:00:00', '2026-08-03 10:00:00'),
    (9200000000552, 9200000000502, 9200000000002, 9200000000203, 900.00, 10, 9, 1,
     '2026-07-31 10:00:00', '2026-08-02 16:00:00');

INSERT INTO promotion_reservations
    (id, reservation_id, activity_id, activity_item_id, tenant_id, consumer_id, request_key,
     quantity, unit_price_snapshot, status, order_id, created_at, updated_at)
VALUES
    (9200000000601, 'A1-RES-A-001', 9200000000501, 9200000000551, 9200000000001, 9200000000901,
     'a1-request-a-001', 2, 30.00, 'ORDER_CREATED', 9200000000301, '2026-08-01 09:00:00', '2026-08-01 09:00:00'),
    (9200000000602, 'A1-RES-A-002', 9200000000501, 9200000000551, 9200000000001, 9200000000902,
     'a1-request-a-002', 1, 30.00, 'ORDER_CREATED', 9200000000302, '2026-08-02 10:00:00', '2026-08-02 10:00:00'),
    (9200000000603, 'A1-RES-A-003', 9200000000501, 9200000000551, 9200000000001, 9200000000903,
     'a1-request-a-003', 1, 30.00, 'FAILED', NULL, '2026-08-02 11:00:00', '2026-08-02 11:00:00'),
    (9200000000604, 'A1-RES-A-004', 9200000000501, 9200000000551, 9200000000001, 9200000000904,
     'a1-request-a-004', 1, 30.00, 'PENDING_ORDER', NULL, '2026-08-03 11:00:00', '2026-08-03 11:00:00'),
    (9200000000605, 'A1-RES-B-001', 9200000000502, 9200000000552, 9200000000002, 9200000000905,
     'a1-request-b-001', 1, 900.00, 'ORDER_CREATED', 9200000000306, '2026-08-02 15:00:00', '2026-08-02 15:00:00');

INSERT INTO after_sale_request
    (id, request_no, order_id, order_item_id, tenant_id, consumer_id, quantity, requested_amount,
     reason, status, merchant_remark, decided_by, decided_at, created_at, updated_at)
VALUES
    (9200000000701, 'A1-AS-A-001', 9200000000301, 9200000000401, 9200000000001, 9200000000901,
     1, 50.00, 'A1 fixed test reason', 'APPROVED', 'approved for fixed test', 9200000000801,
     '2026-08-03 09:00:00', '2026-08-03 08:00:00', '2026-08-03 09:00:00'),
    (9200000000702, 'A1-AS-B-001', 9200000000306, 9200000000404, 9200000000002, 9200000000902,
     1, 999.00, 'tenant B fixed test reason', 'SUBMITTED', NULL, NULL,
     NULL, '2026-08-03 08:00:00', '2026-08-03 08:00:00');
