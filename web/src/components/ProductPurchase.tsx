"use client";

import { apiClient } from "@/lib/client-api";
import { addDemoCartLine } from "@/lib/demo-commerce";
import { currency } from "@/lib/demo-data";
import type { CartItem, ProductDetail, Sku } from "@/lib/types";
import { useRouter } from "next/navigation";
import { useMemo, useState } from "react";

export function ProductPurchase({
  product,
  demo,
  storeId,
  storeName,
}: {
  product: ProductDetail;
  demo: boolean;
  storeId: number;
  storeName: string;
}) {
  const [sku, setSku] = useState<Sku>(product.skus[0]);
  const [quantity, setQuantity] = useState(1);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const router = useRouter();
  const canAdd = useMemo(() => sku.availableStock >= quantity && sku.availableStock > 0, [sku, quantity]);
  async function add() {
    if (demo) {
      addDemoCartLine({ storeId, storeName, productId: product.id, productName: product.name, sku, quantity });
      setMessage("已加入演示购物袋；结算时会按店铺拆分订单。");
      return;
    }
    setLoading(true);
    setMessage("");
    try {
      const availability = await apiClient<{ purchasable: boolean; availableStock: number; message: string }>(
        `/api/backend/public/skus/${sku.id}/availability`,
      );
      if (!availability.purchasable || availability.availableStock < quantity) {
        setMessage(availability.message || "库存不足。");
        return;
      }
      await apiClient<CartItem>("/api/backend/cart/items", {
        method: "POST",
        body: JSON.stringify({ skuId: sku.id, quantity }),
      });
      setMessage("已加入购物袋。");
      router.refresh();
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "操作未完成。");
    } finally {
      setLoading(false);
    }
  }
  return (
    <>
      <p className="price">{currency(sku.salePrice)}</p>
      <span className="eyebrow">选择款式</span>
      <div className="sku-options">
        {product.skus.map((item) => (
          <button
            key={item.id}
            type="button"
            className={sku.id === item.id ? "active" : ""}
            onClick={() => {
              setSku(item);
              setQuantity(1);
            }}
          >
            {item.skuName}
          </button>
        ))}
      </div>
      <div className="purchase-row">
        <div className="stepper">
          <button aria-label="减少购买数量" type="button" onClick={() => setQuantity(Math.max(1, quantity - 1))}>
            −
          </button>
          <span>{quantity}</span>
          <button
            aria-label="增加购买数量"
            type="button"
            disabled={quantity >= sku.availableStock}
            onClick={() => setQuantity(quantity + 1)}
          >
            +
          </button>
        </div>
        <button className="button primary" type="button" disabled={!canAdd || loading} onClick={add}>
          {loading ? "加入中…" : canAdd ? "加入购物袋" : "暂时售罄"}
        </button>
      </div>
      <p className="feedback">库存 {sku.availableStock} 件 · 下单前会再次确认价格与库存</p>
      {message && (
        <output className="feedback">
          {message} {demo && <a href="/cart">查看购物袋 ↗</a>}
        </output>
      )}
    </>
  );
}
