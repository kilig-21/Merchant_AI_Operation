
/*
一、SPU / SKU 设计规范
SPU（标准产品单元）：存储商品公共通用信息，代表商品本体，如商品名称、简介、类目等，统一管理公共商品数据，避免冗余。
SKU（库存保有单元）：存储商品具体可售卖规格，一个SPU可对应多个SKU。负责管理具体规格的价格、规格参数、库存数据。
设计目的：SPU管商品基础信息，SKU管规格、价格、库存，实现数据解耦，减少重复存储。
二、双库存字段设计（核心）
摒弃单一库存字段，采用 可售库存 + 锁定库存 双字段设计，解决电商下单未支付、订单超时、并发超卖等真实业务问题。
1. available_stock 可售库存
用户可直接下单购买的有效库存，是对外售卖的真实库存数量。
2. locked_stock 锁定库存
用户已提交订单、占用库存，但未完成支付的临时锁定库存。下单不等于购买成功，需临时预留库存，防止超卖。
三、库存流转规则
1. 用户下单
可售库存减少，锁定库存增加，临时占用库存。
2. 支付成功
锁定库存清零，商品正式售出，库存扣除完成。
3. 订单超时/取消
锁定库存释放，归还至可售库存，商品恢复可售卖状态。
四、双库存设计价值
- 解决下单未付款导致的库存无法回滚问题，避免库存数据错乱
- 解决秒杀、高并发场景下的库存超卖问题
- 精准区分有效库存、占用库存，贴合真实电商交易流程
五、version 乐观锁设计
数据表增加 version 版本号字段，用于解决高并发库存修改冲突。
更新库存时，仅当数据库版本号与读取版本一致时才更新成功；版本不一致则更新失败，重新查询最新数据，杜绝多人同时改库存导致的数据错误。
六、tenant_id 租户隔离设计
本系统为多商家平台，所有业务表增加 tenant_id 租户字段，用于区分不同商家数据。
所有查询、操作必须携带租户条件，严格隔离各商家数据，防止跨商家数据泄露、数据混淆。
七、整体设计总结
- product_spu：统一管理商品公共信息，去重冗余
- product_sku：管理规格、价格、双库存数据
- available_stock：可直接售卖的有效库存
- locked_stock：未支付订单锁定库存
- version：乐观锁，保障高并发库存安全
- tenant_id：多商家数据隔离
整套设计完整支撑：多商家入驻、商品分级管理、限量促销、秒杀高并发、防超卖、订单库存精准回滚等核心电商场景。
*/


#tenant。它表示“商家租户”，后面商家 A、商家 B 的数据隔离都靠它起头
CREATE TABLE tenant(
    id BIGINT PRIMARY KEY,              #id:租户主键，比如商家 A 是 1，商家 B 是 2。
    name VARCHAR(50) NOT NULL,          #name：商家名称
    status TINYINT NOT NULL DEFAULT 1,  #status:状态，1 可以理解为启用，后面如果商家可以被……冻结用其他值。
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP   #创建时间，默认由 MySQL 自动填当前时间。
);



#也就是说，tenant 是“店铺/商家”，sys_user 是“登录系统的人”。
#这张表表示系统用户，后面会放两类人：
    # 消费者：tenant_id 可以为空
    # 商家员工/管理员：tenant_id 必须指向某个商家
CREATE TABLE sys_user(
    id BIGINT PRIMARY KEY ,
    tenant_id BIGINT NULL COMMENT '消费者可为空，商家员工必须有值',  #用户属于哪个商家。消费者不属于某个商家，所以可以是 NULL。
    username VARCHAR(64) NOT NULL UNIQUE ,                      #登录名，UNIQUE 表示不能重复。
    password_hash VARCHAR(100) NOT NULL,                        #密码哈希，不存明文密码。后面步骤 7 做登录时会用 BCrypt 生成。
    user_type VARCHAR(20) NOT NULL COMMENT 'CONSUMER,MERCHANT_ADMIN,MERCHANT_OPERATOR',#用户类型。比如消费者、商家管理员、商家操作员。
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);


#SPU 是商品主信息。比如“蓝牙耳机”这个商品本身，就是一个 SPU
#product_spu 放商品的公共信息：属于哪个商家、商品名、描述、上架状态。
#SPU表:
CREATE TABLE product_spu(
    id BIGINT PRIMARY KEY ,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant_status (tenant_id, status)     -- 是在表里额外建了一个“查询目录”。
);


#SKU 是具体可购买的商品规格。还是刚才的例子：
#SPU：蓝牙耳机
    #SKU：白色 / 标准版 / 199 元 / 库存 50
    #SKU：黑色 / Pro 版 / 299 元 / 库存 20

CREATE TABLE product_sku (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    spu_id BIGINT NOT NULL,
    sku_name VARCHAR(128) NOT NULL,
    sale_price DECIMAL(10,2) NOT NULL,              #销售价格
    available_stock INT NOT NULL DEFAULT 0,         #库存量:用户能不能买，主要看它。
    locked_stock INT NOT NULL DEFAULT 0,            #锁定库存:用户提交订单但还没支付时，库存先从可售里扣掉，放到锁定里。
    version INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ON_SALE',
    UNIQUE KEY uk_tenant_sku_name (tenant_id, spu_id, sku_name),
    INDEX idx_tenant_spu (tenant_id, spu_id)
);



















