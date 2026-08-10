import { OrderDetailClient } from "@/components/OrderDetailClient";
export default async function OrderDetailPage({ params }: { params: Promise<{ id: string }> }) {
  return <OrderDetailClient id={Number((await params).id)} />;
}
