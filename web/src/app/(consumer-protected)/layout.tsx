import { ServiceUnavailable } from "@/components/ServiceUnavailable";
import { SiteNav } from "@/components/SiteNav";
import { getSessionState } from "@/lib/session";
import { redirect } from "next/navigation";
export default async function ConsumerProtectedLayout({ children }: { children: React.ReactNode }) {
  const session = await getSessionState();
  if (session.status === "anonymous") redirect("/consumer/login");
  if (session.status === "unavailable") return <ServiceUnavailable />;
  if (session.user.userType.startsWith("MERCHANT_")) redirect("/403");
  return (
    <>
      <SiteNav />
      {children}
    </>
  );
}
