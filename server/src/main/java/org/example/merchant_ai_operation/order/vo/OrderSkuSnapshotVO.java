package org.example.merchant_ai_operation.order.vo;

import java.math.BigDecimal;

//不能直接相信购物车里的 skuId 和数量就插订单，还要重新查 SKU 当前状态、SPU 状态、价格、库存、商家 tenantId。
// 所以我们先建一个"下单用 SKU 快照 VO"。
// 注意：这里的 VO 不是严格意义上的前端返回对象，而是 Mapper 查询结果承载对象。
//因为下单时我们不是只要 SKU 表，也不是只要购物车表，而是要把三块信息合起来：

public record OrderSkuSnapshotVO (
        Long cartItemId,
        Long skuId,
        Long tenantId,
        String skuName,
        BigDecimal salePrice,
        Integer quantity,
        Integer availableStock,
        String skuStatus,
        String spuStatus
){

}
