import SparkField from "@/components/ui/SparkField";
import Link from "next/link";
import type { ReactNode } from "react";

export type AuthAudience = "consumer" | "merchant";
export type AuthMode = "login" | "register";

interface AuthSpotlightShellProps {
  audience: AuthAudience;
  mode: AuthMode;
  children: ReactNode;
}

const captionFor = (audience: AuthAudience, mode: AuthMode) => {
  if (audience === "merchant") {
    return mode === "login" ? "MORROW / MERCHANT ACCESS" : "MORROW / MERCHANT REGISTRATION";
  }
  return mode === "login" ? "MORROW / MEMBER ACCESS" : "MORROW / MEMBER REGISTRATION";
};

export function AuthSpotlightShell({ audience, mode, children }: AuthSpotlightShellProps) {
  const label = audience === "merchant" ? "商家" : "用户";
  const action = mode === "login" ? "登录" : "注册";

  return (
    <main className="member-login-page">
      <Link className="member-login-brand" href="/" aria-label="返回 Morrow 首页">
        MORROW <sup>©26</sup>
      </Link>
      <Link className="member-login-back" href="/">
        返回首页 ↗
      </Link>
      <section className={`member-login-stage auth-stage-${mode}`} aria-label={`Morrow ${label}${action}`}>
        <SparkField />
        <div className="member-login-vignette" aria-hidden="true" />
        <div className="member-login-caption" aria-hidden="true">
          <span>{captionFor(audience, mode)}</span>
          <span>QUIET COMMERCE / 2026</span>
        </div>
        {children}
      </section>
    </main>
  );
}
