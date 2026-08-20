import { PlatformShell } from "@/components/PlatformShell";

export default function PlatformOrdersPage() {
  const rows = [
    ["MO-260820-018", "2 家", "¥1,058", "待支付"],
    ["MO-260820-017", "1 家", "¥359", "已支付"],
    ["MO-260820-016", "3 家", "¥1,846", "部分发货"],
    ["MO-260820-015", "2 家", "¥628", "已完成"],
  ];
  return (
    <PlatformShell title="跨店订单" eyebrow="ORDERS / ORCHESTRATION">
      <p className="platform-notice">DEMO / 平台视角展示父订单与店铺子订单关系，不提供真实支付、退款或履约操作。</p>
      <div className="platform-table-wrap">
        <table className="platform-table">
          <thead>
            <tr><th>平台订单</th><th>拆分店铺</th><th>合计金额</th><th>聚合状态</th><th>详情</th></tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row[0]}>
                <td data-label="平台订单"><div><strong>{row[0]}</strong><small>2026-08-20 00:42</small></div></td>
                <td data-label="拆分店铺">{row[1]}</td>
                <td data-label="合计金额">{row[2]}</td>
                <td data-label="聚合状态"><span className="platform-status">{row[3]}</span></td>
                <td data-label="详情"><button disabled type="button">查看拆分</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </PlatformShell>
  );
}
