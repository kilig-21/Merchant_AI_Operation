import { storePresentationFor } from "@/lib/store-presentation";
import type { PublicStoreSummary } from "@/lib/types";
import Image from "next/image";
import Link from "next/link";

export function StoreCard({
  store,
  featured = false,
  wide = false,
}: {
  store: PublicStoreSummary;
  featured?: boolean;
  wide?: boolean;
}) {
  const visual = storePresentationFor(store.id);
  const className = ["market-store-card", featured && "market-store-card--featured", wide && "market-store-card--wide"]
    .filter(Boolean)
    .join(" ");

  return (
    <Link className={className} href={`/stores/${store.id}`}>
      <div className="market-store-card__media" style={{ background: visual.tone }}>
        <Image src={visual.image} alt="" fill sizes={featured ? "(max-width: 760px) 92vw, 52vw" : "(max-width: 760px) 92vw, 32vw"} />
        <span className="market-store-card__badge">PUBLIC STORE</span>
        <span className="market-store-card__index">{visual.label}</span>
      </div>
      <div className="market-store-card__copy">
        <div>
          <span className="eyebrow">PUBLIC DIRECTORY</span>
          <h2>{store.name}</h2>
        </div>
        <p>公开店铺资料将随商家资料能力逐步完善。</p>
        <div className="market-store-card__meta">
          <span>{store.productCount} 件在售</span>
          <strong>进入店铺 ↗</strong>
        </div>
      </div>
    </Link>
  );
}
