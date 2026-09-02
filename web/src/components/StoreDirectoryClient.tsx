"use client";

import type { PublicStoreSummary } from "@/lib/types";
import { useMemo, useState } from "react";
import { StoreCard } from "./StoreCard";

export function StoreDirectoryClient({ stores }: { stores: PublicStoreSummary[] }) {
  const [query, setQuery] = useState("");
  const visible = useMemo(
    () => stores.filter((store) => store.name.toLowerCase().includes(query.trim().toLowerCase())),
    [query, stores],
  );
  const hasFeaturedStore = !query && visible.length > 0;
  const regularStoreCount = visible.length - (hasFeaturedStore ? 1 : 0);

  return (
    <>
      <div className="market-filter-bar">
        <label className="market-search-field">
          <span className="sr-only">搜索店铺</span>
          <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="搜索店铺名称" />
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
      {!visible.length ? <div className="market-empty"><span className="eyebrow">NO MATCH / TRY AGAIN</span><h2>暂时没有找到这间店。</h2><p>换一个店铺名称，或清除条件继续浏览。</p><button type="button" onClick={() => setQuery("")}>清除筛选</button></div> : null}
    </>
  );
}
