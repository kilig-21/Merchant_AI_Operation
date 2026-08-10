"use client";

import { apiClient } from "@/lib/client-api";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { type FormEvent, useMemo, useState } from "react";
import { MerchantShell } from "./MerchantShell";

interface SkuDraft {
  skuName: string;
  salePrice: number;
  availableStock: number;
}

export function MerchantProductCreate() {
  const router = useRouter();
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [publishNow, setPublishNow] = useState(true);
  const [skus, setSkus] = useState<SkuDraft[]>([{ skuName: "标准款", salePrice: 99, availableStock: 10 }]);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const valid = useMemo(
    () =>
      name.trim().length > 0 &&
      skus.every((sku) => sku.skuName.trim().length > 0 && sku.salePrice >= 0 && sku.availableStock >= 0),
    [name, skus],
  );

  function updateSku(index: number, patch: Partial<SkuDraft>) {
    setSkus((current) => current.map((sku, skuIndex) => (skuIndex === index ? { ...sku, ...patch } : sku)));
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!valid || saving) return;
    setSaving(true);
    setError("");
    try {
      const product = await apiClient<{ id: number }>("/api/backend/merchant/products", {
        method: "POST",
        body: JSON.stringify({
          name: name.trim(),
          description: description.trim() || undefined,
        }),
      });
      for (const sku of skus) {
        await apiClient<{ id: number }>(`/api/backend/merchant/products/${product.id}/skus`, {
          method: "POST",
          body: JSON.stringify(sku),
        });
      }
      if (publishNow) {
        await apiClient<null>(`/api/backend/merchant/products/${product.id}/publish`, { method: "POST" });
      }
      router.push("/merchant/products?created=1");
      router.refresh();
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "商品创建失败。");
    } finally {
      setSaving(false);
    }
  }

  return (
    <MerchantShell
      title="新增商品"
      eyebrow="CATALOG / CREATE"
      actions={
        <Link className="button" href="/merchant/products">
          取消
        </Link>
      }
    >
      <form className="create-grid" onSubmit={submit}>
        <div>
          <section className="form-section surface">
            <span className="eyebrow">01 / BASIC INFORMATION</span>
            <h2 className="editorial">商品是什么？</h2>
            <label className="form-field">
              商品名称
              <input
                maxLength={80}
                onChange={(event) => setName(event.target.value)}
                placeholder="例如：晨雾保温杯"
                required
                value={name}
              />
            </label>
            <label className="form-field">
              商品描述
              <textarea
                maxLength={500}
                onChange={(event) => setDescription(event.target.value)}
                placeholder="说清它适合谁、解决什么需要。"
                value={description}
              />
            </label>
          </section>
          <section className="form-section surface">
            <span className="eyebrow">02 / SELLING UNITS</span>
            <h2 className="editorial">有哪些可售款式？</h2>
            {skus.map((sku, index) => (
              <article className="sku-card" key={`${index}-${sku.skuName}`}>
                <div className="sku-card-head">
                  <b>SKU {String(index + 1).padStart(2, "0")}</b>
                  <button
                    disabled={skus.length === 1}
                    onClick={() => setSkus((current) => current.filter((_, skuIndex) => skuIndex !== index))}
                    type="button"
                  >
                    移除
                  </button>
                </div>
                <div className="sku-fields">
                  <label className="form-field">
                    款式名称
                    <input
                      onChange={(event) => updateSku(index, { skuName: event.target.value })}
                      value={sku.skuName}
                    />
                  </label>
                  <label className="form-field">
                    售价（元）
                    <input
                      min="0"
                      onChange={(event) => updateSku(index, { salePrice: Number(event.target.value) })}
                      step="0.01"
                      type="number"
                      value={sku.salePrice}
                    />
                  </label>
                  <label className="form-field">
                    可售库存
                    <input
                      min="0"
                      onChange={(event) => updateSku(index, { availableStock: Number(event.target.value) })}
                      step="1"
                      type="number"
                      value={sku.availableStock}
                    />
                  </label>
                </div>
              </article>
            ))}
            <button
              className="button"
              onClick={() =>
                setSkus((current) => [
                  ...current,
                  { skuName: `款式 ${current.length + 1}`, salePrice: 99, availableStock: 10 },
                ])
              }
              type="button"
            >
              ＋ 增加一个 SKU
            </button>
          </section>
        </div>
        <aside className="publish-panel surface">
          <span className="eyebrow">PUBLISHING</span>
          <h2 className="editorial">发布检查</h2>
          <ul>
            <li className={name ? "done" : ""}>商品名称</li>
            <li className={description ? "done" : ""}>清晰描述</li>
            <li className={valid ? "done" : ""}>完整 SKU</li>
          </ul>
          <label className="publish-switch">
            <input
              checked={publishNow}
              onChange={(event) => setPublishNow(event.target.checked)}
              type="checkbox"
            />
            创建后立即上架
          </label>
          <p>上架后，消费者可以在公共商品页看到它。</p>
          {error && <p className="form-error">{error}</p>}
          <button className="button primary" disabled={!valid || saving} type="submit">
            {saving ? "正在创建…" : "创建商品"}
          </button>
        </aside>
      </form>
    </MerchantShell>
  );
}
