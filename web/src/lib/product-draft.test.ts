import { describe, expect, it } from "vitest";
import { validSkuCollection, validSkuDraft } from "./product-draft";

describe("product draft validation", () => {
  const valid = { skuName: "标准款", salePrice: "99.50", availableStock: "10" };

  it("accepts a valid price and whole-number stock", () => {
    expect(validSkuDraft(valid)).toBe(true);
  });

  it("rejects blank, free, over-precision, and invalid stock values", () => {
    expect(validSkuDraft({ ...valid, salePrice: "" })).toBe(false);
    expect(validSkuDraft({ ...valid, salePrice: "0" })).toBe(false);
    expect(validSkuDraft({ ...valid, salePrice: "1.999" })).toBe(false);
    expect(validSkuDraft({ ...valid, availableStock: "" })).toBe(false);
    expect(validSkuDraft({ ...valid, availableStock: "1.5" })).toBe(false);
  });

  it("rejects duplicate SKU names", () => {
    expect(validSkuCollection([valid, { ...valid, skuName: " 标准款 " }])).toBe(false);
  });
});
