const labels: Record<string, string> = {
  ON_SALE: "上架中",
  OFF_SALE: "已下架",
  DRAFT: "草稿",
  PENDING_PAYMENT: "待支付",
  PAID: "已支付",
  CLOSED: "已关闭",
};
export function StatusPill({ status }: { status: string }) {
  return (
    <span className={`status-pill status-${status.toLowerCase()}`}>
      <i />
      {labels[status] ?? status}
    </span>
  );
}
