import { currency, visualFor } from "@/lib/demo-data";
import type { ProductSummary } from "@/lib/types";
import Image from "next/image";
import Link from "next/link";

export function ProductCard({ product, storeId = 1001 }: { product: ProductSummary; storeId?: number }) {
  const visual = visualFor(product.id);
  return (
    <Link className="product-card" href={`/stores/${storeId}/products/${product.id}`}>
      <div className="product-card-media" style={{ background: visual.tone }}>
        <Image src={visual.image} alt={product.name} fill sizes="(max-width: 720px) 88vw, 33vw" />
      </div>
      <div className="product-card-copy">
        <span>{visual.category}</span>
        <strong>{currency(product.minSalePrice)}</strong>
        <h3>{product.name}</h3>
        <p>{product.description}</p>
      </div>
    </Link>
  );
}
