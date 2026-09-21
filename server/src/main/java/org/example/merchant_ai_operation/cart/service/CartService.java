package org.example.merchant_ai_operation.cart.service;


import org.example.merchant_ai_operation.cart.dto.AddCartItemRequest;
import org.example.merchant_ai_operation.cart.dto.UpdateCartItemRequest;
import org.example.merchant_ai_operation.cart.entity.CartItem;
import org.example.merchant_ai_operation.cart.mapper.CartItemMapper;
import org.example.merchant_ai_operation.cart.vo.CartItemDetailVO;
import org.example.merchant_ai_operation.cart.vo.CartItemVO;
import org.example.merchant_ai_operation.common.BizException;
import org.example.merchant_ai_operation.publicapi.product.mapper.PublicProductMapper;
import org.example.merchant_ai_operation.publicapi.product.vo.PublicSkuAvailabilityVO;
import org.example.merchant_ai_operation.security.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    private final CartItemMapper cartItemMapper;
    private final PublicProductMapper publicProductMapper;
    public CartService(CartItemMapper cartItemMapper, PublicProductMapper publicProductMapper) {
        this.cartItemMapper = cartItemMapper;
        this.publicProductMapper = publicProductMapper;
    }

    public CartItemVO addItem(AddCartItemRequest request){
        Long consumerId = CurrentUser.requiredConsumerId();

        //先看是否还可以买(没有货或者不可购买)
        PublicSkuAvailabilityVO availability = publicProductMapper.selectSkuAvailability(request.skuId());
        if(availability == null || !Boolean.TRUE.equals(availability.purchasable())){
            throw new BizException(409,"商品不可购买");
        }

        //库存不足的情况
        if(availability.availableStock()<request.quantity()){
            throw new BizException(409,"库存不足");
        }

        //这样排除完情况以后就可以网购物车里加了


        //先根据消费者的id和商品的id判断购物车这个商品的购物车是否在
        //在的话就直接合并数据1和1==2
        //否侧直接insert,增加数据
        CartItem existing = cartItemMapper.selectByConsumerIdAndSkuId(consumerId, request.skuId());

        CartItem saved;
        if(existing == null){
            CartItem item = new CartItem();
            item.setId(System.currentTimeMillis());
            item.setConsumerId(consumerId);
            item.setSkuId(request.skuId());
            item.setQuantity(request.quantity());

            int inserted = cartItemMapper.insert(item);

            if (inserted != 1) {
                throw new BizException(500, "加入购物车失败");
            }

            saved = cartItemMapper.selectByIdAndConsumerId(item.getId(), consumerId);
        }else {
            //购物车里的数量加上新加进来的数量;
            int newQuantity = existing.getQuantity() + request.quantity();
            if (availability.availableStock() < newQuantity) {
                throw new BizException(409, "库存不足");
            }

            //直接在已有的列表内加数量进去;
            int updated = cartItemMapper.increaseQuantity(existing.getId(), consumerId, request.quantity());
            if (updated != 1) {
                throw new BizException(500, "更新购物车失败");
            }

            saved = cartItemMapper.selectByIdAndConsumerId(existing.getId(), consumerId);
        }

        return new CartItemVO(saved.getId(), saved.getSkuId(), saved.getQuantity());
    }

    //列出购物车的列表(详情)
    public List<CartItemDetailVO> listItems() {
        Long consumerId = CurrentUser.requiredConsumerId();
        return cartItemMapper.selectDetailsByConsumerId(consumerId);
    }

    //返回购物车商品VO
    public CartItemVO updateQuantity(Long id, UpdateCartItemRequest request){
        Long consumerId = CurrentUser.requiredConsumerId();

        CartItem existing = cartItemMapper.selectByIdAndConsumerId(id, consumerId);
        if (existing == null){
            throw new BizException(404,"购物车不存在");
        }

        PublicSkuAvailabilityVO availability = publicProductMapper.selectSkuAvailability(existing.getSkuId());
        if(availability == null||!Boolean.TRUE.equals(availability.purchasable())){
            throw new BizException(409,"商品不可购买");
        }
        if (availability.availableStock() < request.quantity()) {
            throw new BizException(409, "库存不足");
        }

        int updated = cartItemMapper.updateQuantityByIdAndConsumerId(
                id,
                consumerId,
                request.quantity()
        );
        if (updated != 1) {
            throw new BizException(404, "购物车项不存在");
        }

        CartItem saved = cartItemMapper.selectByIdAndConsumerId(id, consumerId);
        return new CartItemVO(saved.getId(), saved.getSkuId(), saved.getQuantity());

    }

    //删除购物车订单
    public void deleteItem(Long id) {
        Long consumerId = CurrentUser.requiredConsumerId();

        int deleted = cartItemMapper.deleteByIdAndConsumerId(id, consumerId);
        if (deleted != 1) {
            throw new BizException(404, "购物车项不存在");
        }
    }




}
