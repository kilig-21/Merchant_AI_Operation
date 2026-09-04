"use client";

import { apiClient } from "@/lib/client-api";
import { currency } from "@/lib/demo-data";
import type { OrderDetail } from "@/lib/types";
import { useCallback, useEffect, useState } from "react";
import { DemoNotice } from "./DemoNotice";
import { MerchantShell } from "./MerchantShell";
import { RequestFailure } from "./RequestFailure";
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
  const [loadingOrders, setLoadingOrders] = useState(true);
  const [failure, setFailure] = useState<unknown>(null);
  const { user, loading } = useSession();

  const loadOrders = useCallback(async () => {
    if (loading) return;

    setFailure(null);
    setLoadingOrders(true);

    if (user?.isDemo === true) {
      setOrders(previewOrders);
      setPreview(true);
      setLoadingOrders(false);
      return;
    }

    try {
      const nextOrders = await apiClient<OrderDetail[]>("/api/backend/merchant/orders?page=1&size=50");
      setOrders(nextOrders);
      setPreview(false);
    } catch (caught) {
      setOrders([]);
      setPreview(false);
      setFailure(caught);
    } finally {
      setLoadingOrders(false);
    }
  }, [loading, user?.isDemo]);

  useEffect(() => {
    void loadOrders();
  }, [loadOrders]);

  return (
    <MerchantShell title="本店订单" eyebrow="ORDERS / LIVE READ">
      {preview ? <DemoNotice>当前为显式演示会话；订单与金额仅用于展示，不会触发真实操作。</DemoNotice> : null}
      {failure ? (
        <RequestFailure
          error={failure}
          loginHref="/merchant/login?redirect=/merchant/orders"
          onRetry={loadOrders}
          title="商家订单暂时无法读取"
        />
      ) : null}
      {!failure ? (
        <>
          <div className="merchant-toolbar surface">
            <span className="eyebrow">LATEST ORDERS</span>
            <span className="eyebrow">{loadingOrders ? "LOADING" : `${orders.length} RESULTS`}</span>
          </div>
          {loadingOrders ? <div className="empty-state"><p>正在读取本店真实订单…</p></div> : null}
          {!loadingOrders && !orders.length ? (
            <div className="empty-state">
              <h2>当前没有订单。</h2>
              <p>这里仅显示当前商家店铺的真实订单；空列表不代表请求失败。</p>
            </div>
          ) : null}
          {!loadingOrders && orders.length ? (
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
                      <td data-label="订单号"><strong>{order.orderNo}</strong></td>
                      <td data-label="创建时间">{new Date(order.createdAt).toLocaleString("zh-CN")}</td>
                      <td data-label="金额">{currency(order.totalAmount)}</td>
                      <td data-label="状态"><StatusPill status={order.status} /></td>
                      <td data-label="履约"><span className="table-note">履约接口待后续版本提供</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : null}
        </>
      ) : null}
    </MerchantShell>
  );
}
