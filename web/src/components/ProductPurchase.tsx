"use client";

import { apiClient } from "@/lib/client-api";
import { currency } from "@/lib/demo-data";
import type { CartItem, ProductDetail, Sku } from "@/lib/types";
import { useRouter } from "next/navigation";
import { useMemo, useState } from "react";

export function ProductPurchase({ product, demo }: { product: ProductDetail; demo: boolean }) {
  const [sku, setSku] = useState<Sku>(product.skus[0]);
  const [quantity, setQuantity] = useState(1);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const router = useRouter();
  const canAdd = useMemo(() => sku.availableStock >= quantity && sku.availableStock > 0, [sku, quantity]);
  async function add() {
    if (demo) {
      setMessage("演示商品不可加入真实购物袋，请连接后端后再试。");
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
          <button type="button" onClick={() => setQuantity(Math.max(1, quantity - 1))}>
            −
          </button>
          <span>{quantity}</span>
          <button
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
      {message && <p className="feedback">{message}</p>}
    </>
  );
}
