import { PlatformShell } from "@/components/PlatformShell";

export default function PlatformGovernancePage() {
  return (
    <PlatformShell title="内容治理" eyebrow="TRUST / GOVERNANCE">
      <p className="platform-notice">DEMO / 仅用于呈现治理队列与风险分级，不执行下架、封禁或外部通知。</p>
      <section className="governance-grid">{[
        ["商品描述完整性", "12", "需补充尺寸、材质或使用说明"],
        ["图片版权复核", "04", "来源信息不完整，等待商家补充"],
        ["异常价格波动", "03", "价格变化超过近期区间"],
        ["顾客争议升级", "02", "需要平台人工介入查看证据"],
      ].map((item, index) => <article key={item[0]}><span className="eyebrow">RISK 0{index + 1}</span><strong>{item[1]}</strong><h2>{item[0]}</h2><p>{item[2]}</p><button disabled type="button">进入队列</button></article>)}</section>
    </PlatformShell>
  );
}
