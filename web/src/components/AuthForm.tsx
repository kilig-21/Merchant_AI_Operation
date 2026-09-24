"use client";

import { apiClient } from "@/lib/client-api";
import type { SessionUser } from "@/lib/types";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useState } from "react";
import { useSession } from "./SessionProvider";
import type { AuthAudience, AuthMode } from "./auth/AuthSpotlightShell";

type AuthSubmission = "live" | "demo";

interface AuthFormProps {
  audience: AuthAudience;
  mode: AuthMode;
  submission?: AuthSubmission;
}

type FormValues = {
  username: string;
  password: string;
  passwordConfirm: string;
  businessName: string;
  storeName: string;
  contactName: string;
  phone: string;
};

const initialValues: FormValues = {
  username: "",
  password: "",
  passwordConfirm: "",
  businessName: "",
  storeName: "",
  contactName: "",
  phone: "",
};

const copy = {
  consumer: {
    login: {
      eyebrow: "MEMBER / LOGIN",
      title: "用户登录",
      summary: "继续查看购物袋、订单和每一次认真选择。",
      submit: "登录",
      switch: "第一次来到这里？",
      switchAction: "去注册",
    },
    register: {
      eyebrow: "MEMBER / REGISTER",
      title: "用户注册",
      summary: "创建账户，保存购物袋、订单和认真挑选过的日常。",
      submit: "创建用户账户",
      switch: "已经拥有账户？",
      switchAction: "去登录",
    },
  },
  merchant: {
    login: {
      eyebrow: "MERCHANT / LOGIN",
      title: "商家登录",
      summary: "进入经营工作台，继续管理商品、库存与订单。",
      submit: "登录工作台",
      switch: "还没有商家账户？",
      switchAction: "申请注册",
    },
    register: {
      eyebrow: "MERCHANT / REGISTER",
      title: "商家注册",
      summary: "提交基础经营信息，为开通 Morrow 商家工作台做准备。",
      submit: "提交注册申请",
      switch: "已经拥有商家账户？",
      switchAction: "去登录",
    },
  },
} as const;

export function AuthForm({ audience, mode, submission = "live" }: AuthFormProps) {
  const [values, setValues] = useState<FormValues>(initialValues);
  const [error, setError] = useState("");
  const [demoNotice, setDemoNotice] = useState(false);
  const [loading, setLoading] = useState(false);
  const router = useRouter();
  const search = useSearchParams();
  const session = useSession();
  const isLogin = mode === "login";
  const isMerchant = audience === "merchant";
  const spotlightCopy = copy[audience][mode];

  const setValue = <K extends keyof FormValues>(key: K, value: FormValues[K]) => {
    setValues((current) => ({ ...current, [key]: value }));
  };

  const validate = () => {
    if (isLogin) {
      if (!values.username.trim() || !values.password) return "请输入账号和密码。";
      return "";
    }
    if (
      isMerchant &&
      (!values.businessName.trim() || !values.storeName.trim() || !values.contactName.trim())
    ) {
      return "请先补充商家名称、店铺名称和联系人。";
    }
    if (!values.username.trim() || !values.password || !values.passwordConfirm) {
      return "请完整填写注册信息。";
    }
    if (values.username.trim().length < 3 || values.username.trim().length > 64) return "账号长度需为 3 到 64 个字符。";
    if (values.password.length < 6 || values.password.length > 32) return "密码长度需为 6 到 32 个字符。";
    if (values.password !== values.passwordConfirm) return "两次输入的密码不一致。";
    return "";
  };

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setDemoNotice(false);
    const validationError = validate();
    if (validationError) {
      setError(validationError);
      return;
    }
    if (submission === "demo") {
      setDemoNotice(true);
      return;
    }
    setLoading(true);
    try {
      const result = await apiClient<{ user: SessionUser }>(`/api/session/${mode}`, {
        method: "POST",
        body: JSON.stringify({ username: values.username.trim(), password: values.password }),
      });
      if (isMerchant && !result.user.userType.startsWith("MERCHANT_")) {
        await fetch("/api/session/logout", { method: "POST" });
        throw new Error("该账号不是商家账户。");
      }
      if (!isMerchant && result.user.userType.startsWith("MERCHANT_")) {
        await fetch("/api/session/logout", { method: "POST" });
        throw new Error("请使用消费者账户登录。");
      }
      await session.refresh();
      const target = search.get("redirect");
      router.push(target?.startsWith("/") ? target : isMerchant ? "/merchant/dashboard" : "/account");
      router.refresh();
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "操作失败。");
    } finally {
      setLoading(false);
    }
  }

  async function enterDemo() {
    setLoading(true);
    setError("");
    try {
      await apiClient<{ user: SessionUser }>("/api/session/demo", {
        method: "POST",
        body: JSON.stringify({ audience }),
      });
      await session.refresh();
      router.push(isMerchant ? "/merchant/dashboard" : "/stores");
      router.refresh();
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "演示模式暂时无法进入。");
    } finally {
      setLoading(false);
    }
  }

  const switchHref = isMerchant
    ? isLogin
      ? "/merchant/register"
      : "/merchant/login"
    : isLogin
      ? "/consumer/register"
      : "/consumer/login";

  return (
    <form
      className={`auth-form auth-form-spotlight auth-form-${mode}${isMerchant ? " auth-form-merchant" : ""}`}
      onSubmit={submit}
      noValidate
    >
      <div className="auth-member-mark" aria-hidden="true">
        M
      </div>
      <span className="eyebrow">{spotlightCopy.eyebrow}</span>
      <h1>{spotlightCopy.title}</h1>
      <p>{spotlightCopy.summary}</p>

      {!isLogin && isMerchant ? (
        <div className="auth-form-grid">
          <Field
            label="商家名称"
            value={values.businessName}
            onChange={(value) => setValue("businessName", value)}
            placeholder="输入商家名称"
            autoComplete="organization"
          />
          <Field
            label="店铺名称"
            value={values.storeName}
            onChange={(value) => setValue("storeName", value)}
            placeholder="输入店铺名称"
            autoComplete="organization-title"
          />
          <Field
            label="联系人"
            value={values.contactName}
            onChange={(value) => setValue("contactName", value)}
            placeholder="输入联系人姓名"
            autoComplete="name"
          />
          <Field
            label="联系电话"
            value={values.phone}
            onChange={(value) => setValue("phone", value)}
            placeholder="输入联系电话"
            autoComplete="tel"
            type="tel"
          />
        </div>
      ) : null}

      <Field
        label={isMerchant ? "商家账号" : "账号"}
        value={values.username}
        onChange={(value) => setValue("username", value)}
        placeholder={isMerchant ? "输入商家账号" : "输入账号"}
        autoComplete="username"
      />
      <Field
        label="密码"
        value={values.password}
        onChange={(value) => setValue("password", value)}
        type="password"
        autoComplete={isLogin ? "current-password" : "new-password"}
        placeholder="输入密码"
      />
      {!isLogin ? (
        <Field
          label="确认密码"
          value={values.passwordConfirm}
          onChange={(value) => setValue("passwordConfirm", value)}
          type="password"
          autoComplete="new-password"
          placeholder="再次输入密码"
        />
      ) : null}

      {!isLogin && !isMerchant ? (
        <p className="auth-field-note">当前仅支持账号密码注册；邮箱绑定、找回密码和服务条款尚未接入。</p>
      ) : null}
      {error ? (
        <p className="form-error" role="alert">
          {error}
        </p>
      ) : null}
      {demoNotice ? (
        <output className="auth-demo-notice">
          <strong>DEMO / 商家注册接口待接入</strong>
          <span>当前仅完成页面与表单演示，信息尚未提交。</span>
        </output>
      ) : null}
      <button className="button primary" disabled={loading} type="submit">
        {loading ? "处理中…" : spotlightCopy.submit}
      </button>
      {isLogin ? (
        <button className="button auth-demo-entry" disabled={loading} onClick={() => void enterDemo()} type="button">
          {isMerchant ? "进入演示工作台" : "使用演示会员浏览"}
        </button>
      ) : null}
      <div className="auth-secure-note">
        <span>{submission === "demo" ? "DEMO FORM / NO REQUEST" : "SECURE MEMBER ACCESS"}</span>
        <p>
          {submission === "demo"
            ? "此表单只用于展示商家入驻流程，不会发送任何信息。"
            : "会话凭证仅保存在安全 Cookie 中，不会暴露给页面脚本。"}
        </p>
      </div>
      <p className="auth-switch">
        {spotlightCopy.switch} <Link href={switchHref}>{spotlightCopy.switchAction}</Link>
      </p>
      <small className="auth-legal">{submission === "demo" ? "演示商家注册不会提交或保存输入信息。" : "登录会创建必要的会话 Cookie。"}</small>
    </form>
  );
}

function Field({
  label,
  value,
  onChange,
  placeholder,
  autoComplete,
  type = "text",
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
  placeholder: string;
  autoComplete: string;
  type?: string;
}) {
  return (
    <label className="form-field">
      <span>{label}</span>
      <input
        value={value}
        onChange={(event) => onChange(event.target.value)}
        type={type}
        autoComplete={autoComplete}
        placeholder={placeholder}
      />
    </label>
  );
}
