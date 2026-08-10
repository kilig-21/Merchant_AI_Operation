import { ServiceUnavailable } from "@/components/ServiceUnavailable";
import { getSessionState } from "@/lib/session";
import { redirect } from "next/navigation";
export default async function MerchantProtectedLayout({ children }: { children: React.ReactNode }) {
  const session = await getSessionState();
  if (session.status === "anonymous") redirect("/merchant/login");
  if (session.status === "unavailable") return <ServiceUnavailable />;
  if (!session.user.userType.startsWith("MERCHANT_")) redirect("/403");
  return children;
}
