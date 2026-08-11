import { AuthForm } from "@/components/AuthForm";
import { AuthSpotlightShell } from "@/components/auth/AuthSpotlightShell";
import { Suspense } from "react";

export default function MerchantRegisterPage() {
  return (
    <AuthSpotlightShell audience="merchant" mode="register">
      <Suspense>
        <AuthForm audience="merchant" mode="register" submission="demo" />
      </Suspense>
    </AuthSpotlightShell>
  );
}
