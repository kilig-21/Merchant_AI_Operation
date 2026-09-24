"use client";

import { apiClient } from "@/lib/client-api";
import { type SkuDraft, validSkuCollection } from "@/lib/product-draft";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { type FormEvent, useMemo, useRef, useState } from "react";
import { DemoNotice } from "./DemoNotice";
import { MerchantShell } from "./MerchantShell";
import { useSession } from "./SessionProvider";

type SkuEditor = SkuDraft & { draftId: number };

export function MerchantProductCreate() {
  const router = useRouter();
  const { user, loading: sessionLoading } = useSession();
  const isDemo = user?.isDemo === true;
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [publishNow, setPublishNow] = useState(false);
  const [skus, setSkus] = useState<SkuEditor[]>([{ draftId: 1, skuName: "标准款", salePrice: "", availableStock: "" }]);
  const nextDraftId = useRef(2);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [uncertainOutcome, setUncertainOutcome] = useState(false);
  const valid = useMemo(
    () => name.trim().length > 0 && validSkuCollection(skus),
    [name, skus],
  );

  function updateSku(index: number, patch: Partial<SkuDraft>) {
    setSkus((current) => current.map((sku, skuIndex) => (skuIndex === index ? { ...sku, ...patch } : sku)));
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!valid || saving || sessionLoading || isDemo || uncertainOutcome) return;
    setSaving(true);
    setError("");
    let productId: number | null = null;
    let currentStep = "创建商品";
    try {
      const product = await apiClient<{ id: number }>("/api/backend/merchant/products", {
        method: "POST",
        body: JSON.stringify({
          name: name.trim(),
          description: description.trim() || undefined,
        }),
      });
      productId = product.id;
      for (const sku of skus) {
        currentStep = `添加款式「${sku.skuName.trim()}」`;
        await apiClient<{ id: number }>(`/api/backend/merchant/products/${product.id}/skus`, {
          method: "POST",
          body: JSON.stringify({ skuName: sku.skuName.trim(), salePrice: Number(sku.salePrice), availableStock: Number(sku.availableStock) }),
        });
      }
      if (publishNow) {
        currentStep = "上架商品";
        await apiClient<null>(`/api/backend/merchant/products/${product.id}/publish`, { method: "POST" });
      }
      router.push("/merchant/products?created=1");
      router.refresh();
    } catch (caught) {
      const reason = caught instanceof Error ? caught.message : "请求未完成。";
      setError(productId
        ? `商品草稿 #${productId} 已创建，但${currentStep}未确认成功：${reason} 请到商品管理核对，不要在此重新提交。`
        : `创建请求结果未确认：${reason} 请先到商品管理核对，避免重复创建。`);
      setUncertainOutcome(true);
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
      {isDemo ? <DemoNotice>演示账号只能预览商品编辑界面，不能创建或上架真实商品。</DemoNotice> : null}
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
              <article className="sku-card" key={sku.draftId}>
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
                      maxLength={128}
                      onChange={(event) => updateSku(index, { skuName: event.target.value })}
                      value={sku.skuName}
                    />
                  </label>
                  <label className="form-field">
                    售价（元）
                    <input
                      min="0"
                      onChange={(event) => updateSku(index, { salePrice: event.target.value })}
                      step="0.01"
                      type="number"
                      value={sku.salePrice}
                    />
                  </label>
                  <label className="form-field">
                    可售库存
                    <input
                      min="0"
                      onChange={(event) => updateSku(index, { availableStock: event.target.value })}
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
                  { draftId: nextDraftId.current++, skuName: `款式 ${current.length + 1}`, salePrice: "", availableStock: "" },
                ])
              }
              type="button"
            >
              ＋ 增加一个 SKU
            </button>
            <p className="auth-field-note">款式名称不可重复；售价需大于 0 且最多两位小数，库存需为非负整数。</p>
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
          {uncertainOutcome ? <Link className="button" href="/merchant/products">前往商品管理核对 ↗</Link> : null}
          <button className="button primary" disabled={!valid || saving || sessionLoading || isDemo || uncertainOutcome} type="submit">
            {isDemo ? "演示账号不可创建" : uncertainOutcome ? "请先核对创建结果" : saving ? "正在创建…" : "创建商品"}
          </button>
        </aside>
      </form>
    </MerchantShell>
  );
}
