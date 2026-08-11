import { AuthForm } from "@/components/AuthForm";
import { AuthSpotlightShell } from "@/components/auth/AuthSpotlightShell";
import { Suspense } from "react";

export default function LoginPage() {
  return (
    <AuthSpotlightShell audience="consumer" mode="login">
      <Suspense>
        <AuthForm audience="consumer" mode="login" />
      </Suspense>
    </AuthSpotlightShell>
  );
}
