"use client";

import { DemoNotice } from "./DemoNotice";
import { MerchantShell } from "./MerchantShell";
import { useSession } from "./SessionProvider";

export function MerchantSettings() {
  const { user } = useSession();
  return (
    <MerchantShell title="店铺设置" eyebrow="STORE / SETTINGS">
      <DemoNotice>当前后端没有店铺资料写入接口；以下信息仅作静态界面预留。</DemoNotice>
      <div className="settings-list">
        <article>
          <span className="eyebrow">STORE IDENTITY</span>
          <h2 className="editorial">Morrow 日常选物</h2>
          <p>店铺 ID：{user?.tenantId ?? 1001}</p>
        </article>
        <article>
          <span className="eyebrow">OPERATOR</span>
          <h2 className="editorial">{user?.username ?? "商家管理员"}</h2>
          <p>角色：{user?.userType ?? "MERCHANT_ADMIN"}</p>
        </article>
        <article>
          <span className="eyebrow">BRAND LANGUAGE</span>
          <h2 className="editorial">把喜欢的日常，留在明天之前。</h2>
          <p>品牌名称、介绍和视觉字段待后端能力开放后接入。</p>
        </article>
      </div>
    </MerchantShell>
  );
}
