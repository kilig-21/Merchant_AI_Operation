-- Existing concurrency tests expect this historic SKU. Seed only the isolated test database.
INSERT IGNORE INTO product_spu (id, tenant_id, name, status)
VALUES (1784970220000, 1001, 'Upgrade regression product', 'ON_SALE');
INSERT IGNORE INTO product_sku
    (id, tenant_id, spu_id, sku_name, sale_price, available_stock, locked_stock, version, status)
VALUES (1784970220075, 1001, 1784970220000, 'Upgrade regression SKU', 199.00, 100, 0, 0, 'ON_SALE');
