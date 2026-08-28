import { describe, expect, it } from "vitest";
import {
  demoDetailForStore,
  demoMarketplaceProducts,
  demoProductsForStore,
  demoStores,
  storeFor,
} from "./demo-data";

describe("multi-store demo catalog", () => {
  it("keeps a distinct catalog identity for every configured store", () => {
    expect(demoStores).toHaveLength(4);
    expect(new Set(demoStores.map((store) => store.id)).size).toBe(demoStores.length);
    for (const store of demoStores) {
      expect(demoProductsForStore(store.id)).toHaveLength(store.productCount);
      expect(storeFor(store.id).name).toBe(store.name);
    }
  });

  it("uses tenant-scoped SKU identifiers for the same product id", () => {
    const first = demoDetailForStore(1001, 1);
    const second = demoDetailForStore(1002, 1);
    expect(first.skus[0].id).not.toBe(second.skus[0].id);
    expect(first.name).not.toBe(second.name);
  });

  it("adds store identity to every marketplace result", () => {
    expect(demoMarketplaceProducts).toHaveLength(demoStores.reduce((sum, store) => sum + store.productCount, 0));
    expect(demoMarketplaceProducts.every((product) => product.storeId && product.storeName)).toBe(true);
  });
});
