import { AuthForm } from "@/components/AuthForm";
import SparkField from "@/components/ui/SparkField";
import Link from "next/link";
import { Suspense } from "react";

export default function LoginPage() {
  return (
    <main className="member-login-page">
      <Link className="member-login-brand" href="/" aria-label="返回 Morrow 首页">
        MORROW <sup>©26</sup>
      </Link>
      <Link className="member-login-back" href="/">
        返回首页 ↗
      </Link>
      <section className="member-login-stage" aria-label="Morrow 会员登录">
        <SparkField />
        <div className="member-login-vignette" aria-hidden="true" />
        <div className="member-login-caption" aria-hidden="true">
          <span>MORROW / MEMBER ACCESS</span>
          <span>QUIET COMMERCE / 2026</span>
        </div>
        <Suspense>
          <AuthForm mode="login" variant="spotlight" />
        </Suspense>
      </section>
    </main>
  );
}
