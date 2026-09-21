import { PromotionDetailClient } from "@/components/PromotionDetailClient";
import { SiteFooter } from "@/components/SiteFooter";
import { SiteNav } from "@/components/SiteNav";
import { notFound } from "next/navigation";

export default async function PromotionDetailPage({ params }: { params: Promise<{ activityId: string }> }) {
  const { activityId: rawActivityId } = await params;
  const activityId = Number(rawActivityId);
  if (!Number.isSafeInteger(activityId) || activityId <= 0) notFound();
  return <><SiteNav /><PromotionDetailClient activityId={activityId} /><SiteFooter /></>;
}
