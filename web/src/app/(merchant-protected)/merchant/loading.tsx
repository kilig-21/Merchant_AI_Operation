import { MerchantShell } from "@/components/MerchantShell";

export default function MerchantLoading() {
  return (
    <MerchantShell title="正在打开页面" eyebrow="MORROW / LOADING">
      <output className="merchant-route-loading">
        <span aria-hidden="true" className="merchant-route-loading__bar" />
        <span className="merchant-route-loading__message">正在准备工作台页面…</span>
      </output>
    </MerchantShell>
  );
}
