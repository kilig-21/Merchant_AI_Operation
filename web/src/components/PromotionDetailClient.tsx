"use client";

import { apiClient } from "@/lib/client-api";
import type { PromotionReservationDetail, PromotionReservationResult, PublicPromotionDetail } from "@/lib/types";
import Link from "next/link";
import { useEffect, useState } from "react";
import { useSession } from "./SessionProvider";

const money = (value: number) => `¥${Number(value).toFixed(2)}`;
const finalStatuses = new Set(["ORDER_CREATED", "FAILED", "COMPENSATED"]);

export function PromotionDetailClient({ activityId }: { activityId: number }) {
  const { user, loading: sessionLoading } = useSession();
  const [data, setData] = useState<PublicPromotionDetail | null>(null);
  const [reservation, setReservation] = useState<PromotionReservationDetail | null>(null);
  const [reservationId, setReservationId] = useState("");
  const [failure, setFailure] = useState("");
  const [message, setMessage] = useState("");
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    let cancelled = false;
    apiClient<PublicPromotionDetail>(`/api/backend/public/promotions/${activityId}`)
      .then((result) => !cancelled && setData(result))
      .catch((error) => !cancelled && setFailure(error instanceof Error ? error.message : "活动暂时无法读取。"));
    return () => {
      cancelled = true;
    };
  }, [activityId]);

  useEffect(() => {
    if (sessionLoading || !user || user.isDemo || user.userType !== "CONSUMER") return;
    let cancelled = false;
    apiClient<PromotionReservationDetail[]>(`/api/backend/promotions/reservations?activityId=${activityId}`)
      .then((result) => {
        if (cancelled || !result.length) return;
        setReservation(result[0]);
        setReservationId(result[0].reservationId);
      })
      .catch(() => undefined);
    return () => {
      cancelled = true;
    };
  }, [activityId, sessionLoading, user]);

  useEffect(() => {
    if (!reservationId) return;
    let cancelled = false;
    let attempts = 0;
    const poll = async () => {
      try {
        const result = await apiClient<PromotionReservationDetail>(`/api/backend/promotions/reservations/${reservationId}`);
        if (cancelled) return;
        setReservation(result);
        if (finalStatuses.has(result.reservationStatus)) return;
      } catch (error) {
        if (!cancelled) setMessage(error instanceof Error ? error.message : "资格结果暂时无法刷新。");
        return;
      }
      attempts += 1;
      if (!cancelled && attempts < 10) window.setTimeout(() => void poll(), 2_000);
      if (!cancelled && attempts >= 10) setMessage("资格已获得，订单仍在处理中。请稍后刷新此页查看结果。");
    };
    void poll();
    return () => {
      cancelled = true;
    };
  }, [reservationId]);

  async function reserve() {
    if (!data) return;
    setBusy(true);
    setFailure("");
    setMessage("");
    try {
      const requestKey = typeof crypto?.randomUUID === "function" ? crypto.randomUUID() : `${Date.now()}-${Math.random()}`;
      const result = await apiClient<PromotionReservationResult>("/api/backend/promotions/reservations", {
        method: "POST",
        body: JSON.stringify({ activityItemId: data.activity.activityItemId, quantity: 1, requestKey }),
      });
      setReservationId(result.reservationId);
      setMessage("已获得抢购资格，正在创建订单。资格成功不等于订单已经创建。");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "抢购未完成。");
    } finally {
      setBusy(false);
    }
  }

  if (failure) return <main className="page-shell promotion-page"><div className="empty-state"><span className="eyebrow">ACTIVITY / UNAVAILABLE</span><h2>活动暂时无法读取。</h2><p>{failure}</p><Link className="button" href="/promotions">返回活动列表</Link></div></main>;
  if (!data) return <main className="page-shell promotion-page"><div className="empty-state"><p>正在读取真实活动…</p></div></main>;
  const activity = data.activity;
  const consumer = user?.userType === "CONSUMER" && !user.isDemo;
  const canReserve = activity.status === "ACTIVE" && activity.stockStatus === "AVAILABLE" && consumer && !reservationId;

  return <main className="page-shell promotion-page"><Link className="eyebrow promotion-back" href="/promotions">← 返回活动列表</Link><section className="promotion-detail surface"><div><span className="eyebrow">{activity.status === "ACTIVE" ? "LIVE / LIMITED" : "SCHEDULED / UPCOMING"}</span><h1>{activity.name}</h1><p>{activity.productName} · {activity.skuName}</p><dl><div><dt>活动价格</dt><dd>{money(activity.activityPrice)}</dd></div><div><dt>每人限购</dt><dd>{activity.limitPerUser} 件</dd></div><div><dt>活动状态</dt><dd>{activity.stockStatus === "SOLD_OUT" ? "已售罄" : activity.status === "ACTIVE" ? "可尝试抢购" : "尚未开始"}</dd></div></dl></div><aside><span>开始：{new Date(activity.startAt).toLocaleString("zh-CN")}</span><span>结束：{new Date(activity.endAt).toLocaleString("zh-CN")}</span>{!user ? <Link className="button primary" href={`/consumer/login?redirect=/promotions/${activityId}`}>登录后抢购</Link> : null}{user && !consumer ? <p className="form-error">请使用真实消费者账户参与活动。</p> : null}{canReserve ? <button className="button primary" disabled={busy} onClick={() => void reserve()} type="button">{busy ? "资格确认中…" : "尝试抢购"}</button> : null}{reservation ? <ReservationResult reservation={reservation} /> : null}{message ? <p className="promotion-message">{message}</p> : null}</aside></section></main>;
}

function ReservationResult({ reservation }: { reservation: PromotionReservationDetail }) {
  if (reservation.reservationStatus === "ORDER_CREATED" && reservation.orderId) return <div className="promotion-result"><strong>订单已创建</strong><span>订单号 {reservation.orderNo}</span><Link href={`/orders/${reservation.orderId}`}>查看订单 ↗</Link></div>;
  if (reservation.reservationStatus === "PENDING_ORDER") return <div className="promotion-result"><strong>已获得资格</strong><span>订单正在异步创建，请勿重复抢购。</span></div>;
  return <div className="promotion-result"><strong>{reservation.reservationStatus}</strong><span>该资格的订单结果请以服务端状态为准。</span></div>;
}
