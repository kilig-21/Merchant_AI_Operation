"use client";

import { visualFor } from "@/lib/demo-data";
import type { ProductSummary } from "@/lib/types";
import { useMemo, useState } from "react";
import { ProductCard } from "./ProductCard";

export function ProductGridClient({ products, storeId }: { products: ProductSummary[]; storeId: number }) {
  const [category, setCategory] = useState("全部");
  const [query, setQuery] = useState("");
  const categories = ["全部", ...new Set(products.map((item) => visualFor(item.id).category))];
  const visible = useMemo(
    () =>
      products.filter(
        (item) =>
          (category === "全部" || visualFor(item.id).category === category) &&
          `${item.name}${item.description || ""}`.toLowerCase().includes(query.toLowerCase()),
      ),
    [products, category, query],
  );
  return (
    <>
      <div className="catalog-toolbar">
        <div className="filter-tabs">
          {categories.map((item) => (
            <button
              type="button"
              className={category === item ? "active" : ""}
              onClick={() => setCategory(item)}
              key={item}
            >
              {item}
            </button>
          ))}
        </div>
        <input
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="搜索商品"
          aria-label="搜索商品"
        />
      </div>
      {visible.length ? (
        <div className="product-grid">
          {visible.map((product) => (
            <ProductCard key={product.id} product={product} storeId={storeId} />
          ))}
        </div>
      ) : (
        <div className="empty-state">
          <h2>没有找到相符商品</h2>
          <p>换一个关键词或分类试试。</p>
        </div>
      )}
    </>
  );
}
