import { visualFor } from "@/lib/demo-data";
import type { StoreSummary } from "@/lib/types";
import Image from "next/image";
import Link from "next/link";

export function StoreCard({
  store,
  featured = false,
  wide = false,
}: {
  store: StoreSummary;
  featured?: boolean;
  wide?: boolean;
}) {
  const visual = visualFor(store.heroProductId);
  const className = ["market-store-card", featured && "market-store-card--featured", wide && "market-store-card--wide"]
    .filter(Boolean)
    .join(" ");

  return (
    <Link className={className} href={`/stores/${store.id}`}>
      <div className="market-store-card__media" style={{ background: store.tone }}>
        <Image src={visual.image} alt="" fill sizes={featured ? "(max-width: 760px) 92vw, 52vw" : "(max-width: 760px) 92vw, 32vw"} />
        <span className="market-store-card__badge">{store.badge}</span>
        <span className="market-store-card__index">STORE {store.id}</span>
      </div>
      <div className="market-store-card__copy">
        <div>
          <span className="eyebrow">{store.englishName}</span>
          <h2>{store.name}</h2>
        </div>
        <p>{store.tagline}</p>
        <div className="market-store-card__meta">
          <span>{store.location}</span>
          <span>{store.productCount} 件在售</span>
          <span>{store.categories.slice(0, 2).join(" · ")}</span>
          <strong>进入店铺 ↗</strong>
        </div>
      </div>
    </Link>
  );
}
