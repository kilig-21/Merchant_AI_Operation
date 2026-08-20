"use client";

import type { StoreSummary } from "@/lib/types";
import { useMemo, useState } from "react";
import { StoreCard } from "./StoreCard";

export function StoreDirectoryClient({ stores }: { stores: StoreSummary[] }) {
  const categories = ["全部", ...new Set(stores.flatMap((store) => store.categories))];
  const [category, setCategory] = useState("全部");
  const [query, setQuery] = useState("");
  const visible = useMemo(
    () =>
      stores.filter(
        (store) =>
          (category === "全部" || store.categories.includes(category)) &&
          `${store.name}${store.englishName}${store.tagline}${store.description}`
            .toLowerCase()
            .includes(query.trim().toLowerCase()),
      ),
    [category, query, stores],
  );
  const hasFeaturedStore = category === "全部" && !query && visible.length > 0;
  const regularStoreCount = visible.length - (hasFeaturedStore ? 1 : 0);

  return (
    <>
      <div className="market-filter-bar">
        <div className="market-filter-scroll" aria-label="店铺分类">
          {categories.map((item) => (
            <button className={item === category ? "active" : ""} key={item} onClick={() => setCategory(item)} type="button">
              {item}
            </button>
          ))}
        </div>
        <label className="market-search-field">
          <span className="sr-only">搜索店铺</span>
          <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="搜索店铺或生活场景" />
          <span>⌕</span>
        </label>
      </div>
      <div className="market-store-grid">
        {visible.map((store, index) => {
          const featured = hasFeaturedStore && index === 0;
          const wide = !featured && index === visible.length - 1 && regularStoreCount % 2 === 1;
          return <StoreCard featured={featured} key={store.id} store={store} wide={wide} />;
        })}
      </div>
      {!visible.length ? (
        <div className="market-empty">
          <span className="eyebrow">NO MATCH / TRY AGAIN</span>
          <h2>暂时没有找到这间店。</h2>
          <p>换一个关键词，或者回到“全部”继续慢慢看。</p>
          <button type="button" onClick={() => { setCategory("全部"); setQuery(""); }}>
            清除筛选
          </button>
        </div>
      ) : null}
    </>
  );
}
