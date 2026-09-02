"use client";

import { useState } from "react";
import { DemoNotice } from "./DemoNotice";
import { MerchantShell } from "./MerchantShell";
import { useSession } from "./SessionProvider";

const campaigns = [
  { id: "CMP-2608-01", name: "安静工作周", type: "满减", window: "08.20 — 08.27", status: "进行中", reach: "1,284" },
  { id: "CMP-2608-02", name: "新客第一次选择", type: "店铺券", window: "长期", status: "已排期", reach: "—" },
  { id: "CMP-2607-04", name: "周末轻装出发", type: "专题", window: "07.18 — 07.21", status: "已结束", reach: "3,612" },
];

export function MerchantMarketing() {
  const { user, loading } = useSession();
  const [status, setStatus] = useState("全部");
  const visible = campaigns.filter((campaign) => status === "全部" || campaign.status === status);
  if (loading) return <MerchantShell title="营销活动" eyebrow="GROWTH / CAMPAIGNS"><div className="empty-state"><p>正在确认会话状态…</p></div></MerchantShell>;
  if (user?.isDemo !== true) return <MerchantShell title="营销活动" eyebrow="GROWTH / CAMPAIGNS"><div className="empty-state"><span className="eyebrow">LIVE SERVICE / PENDING</span><h2>真实营销活动尚未接入。</h2><p>当前不会展示或发布演示优惠活动。</p></div></MerchantShell>;
  return (
    <MerchantShell title="营销活动" eyebrow="GROWTH / CAMPAIGNS" actions={<button className="button primary" disabled type="button">＋ 创建活动</button>}>
      <DemoNotice>营销接口尚未接入；这里用于确认活动信息结构和工作流，不会发布真实优惠。</DemoNotice>
      <section className="merchant-kicker-grid">
        <article><span>本月活动</span><strong>03</strong><small>1 个正在进行</small></article>
        <article><span>活动触达</span><strong>4.8k</strong><small>演示估算</small></article>
        <article><span>优惠使用</span><strong>12.4%</strong><small>演示转化</small></article>
      </section>
      <div className="merchant-toolbar surface">
        <div className="merchant-tab-list">
          {["全部", "进行中", "已排期", "已结束"].map((item) => <button className={status === item ? "active" : ""} key={item} onClick={() => setStatus(item)} type="button">{item}</button>)}
        </div>
        <span className="eyebrow">{visible.length} CAMPAIGNS</span>
      </div>
      <div className="table-scroll">
        <table className="data-table">
          <thead><tr><th>活动</th><th>形式</th><th>周期</th><th>状态</th><th>触达</th></tr></thead>
          <tbody>{visible.map((campaign) => <tr key={campaign.id}><td><strong>{campaign.name}</strong><br /><small>{campaign.id}</small></td><td>{campaign.type}</td><td>{campaign.window}</td><td><span className="status-pill">{campaign.status}</span></td><td>{campaign.reach}</td></tr>)}</tbody>
        </table>
      </div>
    </MerchantShell>
  );
}
