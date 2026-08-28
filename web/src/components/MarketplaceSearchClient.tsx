"use client";

import { ProductCard } from "@/components/ProductCard";
import { demoStores, visualFor } from "@/lib/demo-data";
import type { MarketplaceProduct } from "@/lib/types";
import { useMemo, useState } from "react";

export function MarketplaceSearchClient({ products, initialQuery = "" }: { products: MarketplaceProduct[]; initialQuery?: string }) {
  const [query, setQuery] = useState(initialQuery);
  const [storeId, setStoreId] = useState("all");
  const [category, setCategory] = useState("全部");
  const categories = ["全部", ...new Set(products.map((product) => visualFor(product.id).category))];
  const visible = useMemo(
    () =>
      products.filter(
        (product) =>
          (storeId === "all" || product.storeId === Number(storeId)) &&
          (category === "全部" || visualFor(product.id).category === category) &&
          `${product.name}${product.description ?? ""}${product.storeName}`.toLowerCase().includes(query.trim().toLowerCase()),
      ),
    [category, products, query, storeId],
  );

  return (
    <>
      <section className="market-search-panel">
        <label className="market-search-panel__input">
          <span>搜索整个 Morrow 市集</span>
          <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="商品、店铺或生活场景" />
        </label>
        <label>
          <span>店铺</span>
          <select value={storeId} onChange={(event) => setStoreId(event.target.value)}>
            <option value="all">全部店铺</option>
            {demoStores.map((store) => <option value={store.id} key={store.id}>{store.name}</option>)}
          </select>
        </label>
        <label>
          <span>分类</span>
          <select value={category} onChange={(event) => setCategory(event.target.value)}>
            {categories.map((item) => <option value={item} key={item}>{item}</option>)}
          </select>
        </label>
      </section>
      <div className="market-results-head">
        <span className="eyebrow">{visible.length} OBJECTS / {storeId === "all" ? "ALL STORES" : `STORE ${storeId}`}</span>
        {(query || storeId !== "all" || category !== "全部") && (
          <button type="button" onClick={() => { setQuery(""); setStoreId("all"); setCategory("全部"); }}>清除条件</button>
        )}
      </div>
      {visible.length ? (
        <div className="product-grid market-product-grid">
          {visible.map((product) => (
            <ProductCard key={`${product.storeId}-${product.id}`} product={product} storeId={product.storeId} storeName={product.storeName} />
          ))}
        </div>
      ) : (
        <div className="market-empty">
          <span className="eyebrow">0 RESULTS</span>
          <h2>没有找到相符的物件。</h2>
          <p>试试更短的关键词，或清除店铺与分类条件。</p>
        </div>
      )}
    </>
  );
}
