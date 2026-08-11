import { AuthForm } from "@/components/AuthForm";
import { AuthSpotlightShell } from "@/components/auth/AuthSpotlightShell";
import { Suspense } from "react";

export default function MerchantLoginPage() {
  return (
    <AuthSpotlightShell audience="merchant" mode="login">
      <Suspense>
        <AuthForm audience="merchant" mode="login" />
      </Suspense>
    </AuthSpotlightShell>
  );
}
