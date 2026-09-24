export interface SkuDraft {
  skuName: string;
  salePrice: string;
  availableStock: string;
}

export function validSkuDraft(sku: SkuDraft) {
  const price = Number(sku.salePrice);
  const stock = Number(sku.availableStock);
  return sku.skuName.trim().length > 0 && sku.skuName.trim().length <= 128
    && /^\d+(\.\d{1,2})?$/.test(sku.salePrice) && price >= 0.01 && price <= 99_999_999.99
    && /^\d+$/.test(sku.availableStock) && Number.isSafeInteger(stock) && stock <= 2_147_483_647;
}

export function validSkuCollection(skus: SkuDraft[]) {
  const names = skus.map((sku) => sku.skuName.trim().toLocaleLowerCase());
  return skus.length > 0 && skus.every(validSkuDraft) && new Set(names).size === names.length;
}
