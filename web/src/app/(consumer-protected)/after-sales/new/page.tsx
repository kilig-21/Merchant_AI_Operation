import { AfterSalesCreateClient } from "@/components/AccountServices";
import { Suspense } from "react";

export default function NewAfterSalesPage() {
  return <Suspense fallback={<main className="page-shell"><div className="empty-state"><p>正在准备申请表…</p></div></main>}><AfterSalesCreateClient /></Suspense>;
}
