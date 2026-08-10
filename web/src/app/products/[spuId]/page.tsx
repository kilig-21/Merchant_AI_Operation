import { redirect } from "next/navigation";
export default async function LegacyProduct({ params }: { params: Promise<{ spuId: string }> }) {
  redirect(`/stores/1001/products/${(await params).spuId}`);
}
