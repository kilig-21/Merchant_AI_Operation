package org.example.merchant_ai_operation.inventory.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.example.merchant_ai_operation.inventory.entity.InventoryMovement;

@Mapper
public interface InventoryMovementMapper {

    @Insert("""
            INSERT INTO inventory_movement (
                tenant_id,
                sku_id,
                business_type,
                business_no,
                available_change,
                locked_change,
                available_after,
                locked_after
            )
            VALUES (
                #{tenantId},
                #{skuId},
                #{businessType},
                #{businessNo},
                #{availableChange},
                #{lockedChange},
                #{availableAfter},
                #{lockedAfter}
            )
            """)
    //把 Java 对象里的属性值，插入到数据库对应字段里。
    //也就是：
        //movement.tenantId -> tenant_id
        //movement.skuId -> sku_id
        //movement.businessType -> business_type
    int insert(InventoryMovement movement);
}