"use client";

import { apiClient } from "@/lib/client-api";
import type { SessionUser } from "@/lib/types";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useState } from "react";
import { useSession } from "./SessionProvider";

interface AuthFormProps {
  mode: "login" | "register";
  merchant?: boolean;
  variant?: "default" | "spotlight";
}

export function AuthForm({ mode, merchant = false, variant = "default" }: AuthFormProps) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const router = useRouter();
  const search = useSearchParams();
  const session = useSession();
  async function submit(event: React.FormEvent) {
    event.preventDefault();
    if (!username || !password) {
      setError("请输入账号和密码。");
      return;
    }
    setLoading(true);
    setError("");
    try {
      const result = await apiClient<{ user: SessionUser }>(`/api/session/${mode}`, {
        method: "POST",
        body: JSON.stringify({ username, password }),
      });
      if (merchant && !result.user.userType.startsWith("MERCHANT_")) {
        await fetch("/api/session/logout", { method: "POST" });
        throw new Error("该账号不是商家账户。");
      }
      if (!merchant && result.user.userType.startsWith("MERCHANT_")) {
        await fetch("/api/session/logout", { method: "POST" });
        throw new Error("请使用消费者账户登录。");
      }
      await session.refresh();
      const target = search.get("redirect");
      router.push(target?.startsWith("/") ? target : merchant ? "/merchant/dashboard" : "/account");
      router.refresh();
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "操作失败。");
    } finally {
      setLoading(false);
    }
  }
  const login = mode === "login";
  const spotlight = variant === "spotlight";
  return (
    <form className={`auth-form${spotlight ? " auth-form-spotlight" : ""}`} onSubmit={submit}>
      {spotlight ? (
        <div className="auth-member-mark" aria-hidden="true">
          M
        </div>
      ) : null}
      <span className="eyebrow">
        {merchant ? "MERCHANT / ACCESS" : login ? "MEMBER / LOGIN" : "MEMBER / REGISTER"}
      </span>
      <h1>
        {merchant
          ? "欢迎回来。"
          : spotlight
            ? "登录 Morrow"
            : login
              ? "继续你的选择。"
              : "从今天开始，慢慢选。"}
      </h1>
      <p>
        {merchant
          ? "使用商家账户进入经营工作台。"
          : spotlight
            ? "继续查看购物袋、订单和每一次认真选择。"
            : login
              ? "登录后查看购物袋与订单状态。"
              : "创建账户，保存每一次认真选择。"}
      </p>
      <label className="form-field">
        <span>账号</span>
        <input
          value={username}
          onChange={(event) => setUsername(event.target.value)}
          autoComplete="username"
          placeholder="输入账号"
        />
      </label>
      <label className="form-field">
        <span>密码</span>
        <input
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          type="password"
          autoComplete={login ? "current-password" : "new-password"}
          placeholder="输入密码"
        />
      </label>
      {error && <p className="form-error">{error}</p>}
      <button className="button primary" disabled={loading} type="submit">
        {loading ? "处理中…" : merchant ? "登录工作台" : login ? "登录" : "创建账户"}
      </button>
      {spotlight ? (
        <div className="auth-secure-note">
          <span>SECURE MEMBER ACCESS</span>
          <p>会话凭证仅保存在安全 Cookie 中，不会暴露给页面脚本。</p>
        </div>
      ) : null}
      {!merchant && (
        <p className="auth-switch">
          {login ? "第一次来到这里？" : "已经有账户？"}{" "}
          <Link href={login ? "/consumer/register" : "/consumer/login"}>{login ? "去注册" : "去登录"}</Link>
        </p>
      )}
      {spotlight ? (
        <small className="auth-legal">继续即表示你同意以必要的会话信息完成账户登录。</small>
      ) : null}
    </form>
  );
}
