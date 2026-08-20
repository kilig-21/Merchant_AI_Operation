import { AfterSalesDetailClient } from "@/components/AccountServices";

export default async function AfterSalesDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return <AfterSalesDetailClient id={Number(id)} />;
}
