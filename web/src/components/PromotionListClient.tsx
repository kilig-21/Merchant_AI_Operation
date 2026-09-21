"use client";

import { apiClient } from "@/lib/client-api";
import type { PublicPromotionActivity, PublicPromotionList } from "@/lib/types";
import Link from "next/link";
import { useEffect, useState } from "react";

const money = (value: number) => `¥${Number(value).toFixed(2)}`;

function phase(activity: PublicPromotionActivity, serverTime: string, current: number) {
  const offset = Date.parse(serverTime) - Date.now();
  const remaining = Date.parse(activity.startAt) - (current + offset);
  if (activity.status === "ACTIVE") return "正在进行";
  if (remaining <= 0) return "等待服务端更新状态";
  const minutes = Math.ceil(remaining / 60_000);
  return `${minutes} 分钟后开始`;
}

export function PromotionListClient() {
  const [data, setData] = useState<PublicPromotionList | null>(null);
  const [failure, setFailure] = useState("");
  const [now, setNow] = useState(Date.now());

  useEffect(() => {
    let cancelled = false;
    apiClient<PublicPromotionList>("/api/backend/public/promotions")
      .then((result) => !cancelled && setData(result))
      .catch((error) => !cancelled && setFailure(error instanceof Error ? error.message : "活动暂时无法读取。"));
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    const timer = window.setInterval(() => setNow(Date.now()), 30_000);
    return () => window.clearInterval(timer);
  }, []);

  return (
    <main className="page-shell promotion-page">
      <header className="page-intro"><div><span className="eyebrow">LIMITED / REAL-TIME</span><h1>限量活动</h1></div><p>价格、活动时间与可售状态来自服务端。倒计时以响应中的服务器时间校准，最终资格由抢购接口原子确认。</p></header>
      {failure ? <div className="empty-state"><span className="eyebrow">SERVICE / UNAVAILABLE</span><h2>活动暂时无法读取。</h2><p>{failure}</p><button className="button" onClick={() => window.location.reload()} type="button">重新读取</button></div> : null}
      {!failure && !data ? <div className="empty-state"><p>正在读取真实活动…</p></div> : null}
      {!failure && data && !data.activities.length ? <div className="empty-state"><span className="eyebrow">NO ACTIVE CAMPAIGNS</span><h2>暂时没有公开活动。</h2><p>已排期和进行中的活动会在这里显示。</p></div> : null}
      {data?.activities.length ? <section className="promotion-grid">{data.activities.map((activity) => <Link className="promotion-card surface" href={`/promotions/${activity.activityId}`} key={activity.activityId}><div><span className="eyebrow">{activity.status === "ACTIVE" ? "LIVE NOW" : "UPCOMING"}</span><h2>{activity.name}</h2><p>{activity.productName} · {activity.skuName}</p></div><div className="promotion-card__price"><strong>{money(activity.activityPrice)}</strong><span>每人限购 {activity.limitPerUser} 件</span></div><footer><span className={activity.stockStatus === "SOLD_OUT" ? "promotion-state sold" : "promotion-state"}>{activity.stockStatus === "SOLD_OUT" ? "已售罄" : phase(activity, data.serverTime, now)}</span><span>查看活动 ↗</span></footer></Link>)}</section> : null}
    </main>
  );
}
