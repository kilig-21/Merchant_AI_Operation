"use client";

import { apiClient } from "@/lib/client-api";
import { currency } from "@/lib/demo-data";
import { appendUniquePage, hasNextPage } from "@/lib/pagination";
import type { OrderDetail } from "@/lib/types";
import { useCallback, useEffect, useState } from "react";
import { DemoNotice } from "./DemoNotice";
import { MerchantShell } from "./MerchantShell";
import { RequestFailure } from "./RequestFailure";
import { StatusPill } from "./StatusPill";
import { useSession } from "./SessionProvider";

const PAGE_SIZE = 50;

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
  const [page, setPage] = useState(1);
  const [hasMore, setHasMore] = useState(false);
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
      setHasMore(false);
      setLoadingOrders(false);
      return;
    }

    try {
      const nextOrders = await apiClient<OrderDetail[]>(`/api/backend/merchant/orders?page=${page}&size=${PAGE_SIZE}`);
      setOrders((current) => page === 1 ? nextOrders : appendUniquePage(current, nextOrders, (order) => order.id));
      setHasMore(hasNextPage(nextOrders.length, PAGE_SIZE));
      setPreview(false);
    } catch (caught) {
      if (page === 1) setOrders([]);
      setPreview(false);
      setFailure(caught);
    } finally {
      setLoadingOrders(false);
    }
  }, [loading, user?.isDemo, page]);

  useEffect(() => {
    void loadOrders();
  }, [loadOrders]);

  return (
    <MerchantShell title="本店订单" eyebrow="ORDERS / LIVE READ">
      {preview ? <DemoNotice>当前为显式演示会话；订单与金额仅用于展示，不会触发真实操作。</DemoNotice> : null}
      {failure && page === 1 ? (
        <RequestFailure
          error={failure}
          loginHref="/merchant/login?redirect=/merchant/orders"
          onRetry={loadOrders}
          title="商家订单暂时无法读取"
        />
      ) : null}
      {!failure || page > 1 ? (
        <>
          <div className="merchant-toolbar surface">
            <span className="eyebrow">LATEST ORDERS</span>
            <span className="eyebrow">{loadingOrders && page === 1 ? "LOADING" : `${orders.length} LOADED`}</span>
          </div>
          {loadingOrders && page === 1 ? <div className="empty-state"><p>正在读取本店真实订单…</p></div> : null}
          {!loadingOrders && !orders.length ? (
            <div className="empty-state">
              <h2>当前没有订单。</h2>
              <p>这里仅显示当前商家店铺的真实订单；空列表不代表请求失败。</p>
            </div>
          ) : null}
          {orders.length ? (
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
          {page > 1 && failure ? <p className="form-error" role="alert">后续订单暂时无法读取。请重试。</p> : null}
          {!preview && hasMore ? <div className="merchant-toolbar"><button className="button" disabled={loadingOrders} onClick={() => failure ? void loadOrders() : setPage((value) => value + 1)} type="button">{loadingOrders ? "加载中…" : failure ? "重试加载更多" : "加载更多订单"}</button></div> : null}
        </>
      ) : null}
    </MerchantShell>
  );
}
