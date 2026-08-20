import { PlatformShell } from "@/components/PlatformShell";
import { demoStores } from "@/lib/demo-data";

export default function PlatformMerchantsPage() {
  return (
    <PlatformShell title="商家审核" eyebrow="TENANTS / APPROVAL">
      <p className="platform-notice">DEMO / 审核按钮保持只读，避免伪造已完成的商家准入操作。</p>
      <div className="platform-table-wrap">
        <table className="platform-table">
          <thead>
            <tr><th>申请主体</th><th>所在地</th><th>经营分类</th><th>材料</th><th>状态</th><th>操作</th></tr>
          </thead>
          <tbody>
            {demoStores.map((store, index) => (
              <tr key={store.id}>
                <td data-label="申请主体"><div><strong>{store.name}</strong><small>TENANT {store.id}</small></div></td>
                <td data-label="所在地">{store.location}</td>
                <td data-label="经营分类">{store.categories.slice(0, 2).join(" / ")}</td>
                <td data-label="材料">{index === 0 ? "已核验" : "待复核"}</td>
                <td data-label="状态"><span className="platform-status">{index === 0 ? "已通过" : "审核中"}</span></td>
                <td data-label="操作"><button disabled type="button">查看材料</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </PlatformShell>
  );
}
