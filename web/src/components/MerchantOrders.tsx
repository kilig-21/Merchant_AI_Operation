"use client";

import { apiClient } from "@/lib/client-api";
import { currency } from "@/lib/demo-data";
import type { OrderDetail } from "@/lib/types";
import { useEffect, useState } from "react";
import { DemoNotice } from "./DemoNotice";
import { MerchantShell } from "./MerchantShell";
import { StatusPill } from "./StatusPill";
import { useSession } from "./SessionProvider";

const previewOrders: OrderDetail[] = [
  {
    id: 95021,
    orderNo: "MO-20260809-95021",
    tenantId: 1001,
    status: "PAID",
    totalAmount: 328,
    expireAt: "2026-08-09T12:00:00+08:00",
    createdAt: "2026-08-09T10:42:00+08:00",
    items: [],
  },
  {
    id: 95020,
    orderNo: "MO-20260809-95020",
    tenantId: 1001,
    status: "PENDING_PAYMENT",
    totalAmount: 168,
    expireAt: "2026-08-09T11:00:00+08:00",
    createdAt: "2026-08-09T10:28:00+08:00",
    items: [],
  },
];

export function MerchantOrders() {
  const [orders, setOrders] = useState<OrderDetail[]>([]);
  const [preview, setPreview] = useState(false);
  const { user, loading } = useSession();
  useEffect(() => {
    if (loading) return;
    if ((user?.id ?? 0) >= 99000) {
      setOrders(previewOrders);
      setPreview(true);
      return;
    }
    apiClient<OrderDetail[]>("/api/backend/merchant/orders?page=1&size=50")
      .then(setOrders)
      .catch(() => {
        setOrders(previewOrders);
        setPreview(true);
      });
  }, [loading, user?.id]);
  return (
    <MerchantShell title="订单预览" eyebrow="ORDERS / PREVIEW">
      {preview && <DemoNotice>后端暂未提供商家订单接口；这里仅呈现静态布局，不伪造履约操作。</DemoNotice>}
      <div className="merchant-toolbar surface">
        <span className="eyebrow">LATEST ORDERS</span>
        <span className="eyebrow">{orders.length} RESULTS</span>
      </div>
      <div className="table-scroll">
        <table className="data-table data-table--responsive">
          <thead>
            <tr>
              <th>订单号</th>
              <th>创建时间</th>
              <th>金额</th>
              <th>状态</th>
              <th>履约</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((order) => (
              <tr key={order.id}>
                <td data-label="订单号">
                  <strong>{order.orderNo}</strong>
                </td>
                <td data-label="创建时间">{new Date(order.createdAt).toLocaleString("zh-CN")}</td>
                <td data-label="金额">{currency(order.totalAmount)}</td>
                <td data-label="状态">
                  <StatusPill status={order.status} />
                </td>
                <td data-label="履约">
                  <button disabled type="button">
                    等待接口
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </MerchantShell>
  );
}
