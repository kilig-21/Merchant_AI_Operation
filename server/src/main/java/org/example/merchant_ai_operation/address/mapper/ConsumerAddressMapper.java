package org.example.merchant_ai_operation.address.mapper;

import org.apache.ibatis.annotations.*;
import org.example.merchant_ai_operation.address.dto.CreateAddressRequest;
import org.example.merchant_ai_operation.address.dto.UpdateAddressRequest;
import org.example.merchant_ai_operation.address.vo.ConsumerAddressVO;
import java.util.List;


@Mapper
public interface ConsumerAddressMapper {

    @Select("""
            SELECT
                id,
                receiver_name AS receiverName,
                receiver_phone AS receiverPhone,
                province,
                city,
                district,
                detail_address AS detailAddress,
                is_default AS isDefault,
                created_at AS createdAt,
                updated_at AS updatedAt
            FROM consumer_address
            WHERE consumer_id = #{consumerId}
            ORDER BY is_default DESC, updated_at DESC
            """)
    //查询当前消费者的地址
    List<ConsumerAddressVO> selectByConsumerId(@Param("consumerId") Long consumerId);

    @Update("""
            UPDATE consumer_address
            SET is_default = 0
            WHERE consumer_id = #{consumerId}
            """)
    //更新默认地址
    void clearDefaultByConsumerId(@Param("consumerId") Long consumerId);

    @Insert("""
            INSERT INTO consumer_address (
                consumer_id,
                receiver_name,
                receiver_phone,
                province,
                city,
                district,
                detail_address,
                is_default
            ) VALUES (
                #{consumerId},
                #{request.receiverName},
                #{request.receiverPhone},
                #{request.province},
                #{request.city},
                #{request.district},
                #{request.detailAddress},
                COALESCE(#{request.isDefault}, 0)
            )
            """)
    //讲地址数据记进数据库里了 -> 新增一条数据库
    int insert(
            @Param("consumerId") Long consumerId,
            @Param("request") CreateAddressRequest request
    );

    @Update("""
            UPDATE consumer_address
            SET receiver_name = #{request.receiverName},
                receiver_phone = #{request.receiverPhone},
                province = #{request.province},
                city = #{request.city},
                district = #{request.district},
                detail_address = #{request.detailAddress},
                is_default = COALESCE(#{request.isDefault}, is_default)
            WHERE id = #{id}
              AND consumer_id = #{consumerId}
            """)
    //更新当前消费者的地址信息
    int updateByIdAndConsumerId(
            @Param("id") Long id,
            @Param("consumerId") Long consumerId,
            @Param("request") UpdateAddressRequest request
    );

    @Delete("""
            DELETE FROM consumer_address
            WHERE id = #{id}
              AND consumer_id = #{consumerId}
            """)
    //消费者删除地址
    int deleteByIdAndConsumerId(
            @Param("id") Long id,
            @Param("consumerId") Long consumerId
    );

    /*
    打开地址列表
    → selectByConsumerId()

    修改/删除/设置默认/创建订单快照
    → selectByIdAndConsumerId()
    */

    @Select("""
            SELECT
                id,
                receiver_name AS receiverName,
                receiver_phone AS receiverPhone,
                province,
                city,
                district,
                detail_address AS detailAddress,
                is_default AS isDefault,
                created_at AS createdAt,
                updated_at AS updatedAt
            FROM consumer_address
            WHERE id = #{id}
              AND consumer_id = #{consumerId}
            """)
    //查询当前消费者指定的某一条地址，并同时验证归属
    ConsumerAddressVO selectByIdAndConsumerId(
            @Param("id") Long id,
            @Param("consumerId") Long consumerId
    );



}