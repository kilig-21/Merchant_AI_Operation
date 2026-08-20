import { DemoNotice } from "./DemoNotice";
import { MerchantShell } from "./MerchantShell";

const segments = [
  { name: "第一次来到店里", count: 486, share: "46%", note: "近 30 天首次下单或收藏" },
  { name: "持续回来的顾客", count: 318, share: "30%", note: "完成两笔及以上订单" },
  { name: "沉默超过 60 天", count: 142, share: "13%", note: "曾购买但近期未互动" },
  { name: "高意向未成交", count: 116, share: "11%", note: "多次加购但尚未下单" },
];

export function MerchantCustomers() {
  return (
    <MerchantShell title="顾客洞察" eyebrow="AUDIENCE / INSIGHTS">
      <DemoNotice>顾客分群为演示数据；未接入真实用户资料，也不提供导出或触达操作。</DemoNotice>
      <section className="customer-overview surface">
        <div><span className="eyebrow">KNOWN CUSTOMERS</span><strong>1,062</strong><p>过去 90 天与店铺发生过有效互动的顾客。</p></div>
        <div className="customer-retention"><span>30 天复购率</span><strong>24.8%</strong><i style={{ width: "24.8%" }} /></div>
      </section>
      <section className="customer-segments">
        {segments.map((segment, index) => <article key={segment.name}><span className="eyebrow">SEGMENT {String(index + 1).padStart(2, "0")}</span><div><h2>{segment.name}</h2><strong>{segment.share}</strong></div><p>{segment.note}</p><footer><span>{segment.count} 人</span><button disabled type="button">查看人群</button></footer></article>)}
      </section>
    </MerchantShell>
  );
}
