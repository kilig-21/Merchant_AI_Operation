import { DEMO_SESSION_COOKIE, SESSION_COOKIE } from "@/lib/session";
import type { ApiEnvelope, SessionUser } from "@/lib/types";
import { NextResponse } from "next/server";

export async function POST(request: Request) {
  const body = (await request.json().catch(() => ({}))) as { audience?: string };
  const audience = body.audience === "merchant" ? "merchant" : "consumer";
  const user: SessionUser =
    audience === "merchant"
      ? { id: 99002, username: "演示店主", userType: "MERCHANT_ADMIN", tenantId: 1001 }
      : { id: 99001, username: "演示会员", userType: "CONSUMER", tenantId: null };
  const response = NextResponse.json<ApiEnvelope<{ user: SessionUser }>>({
    code: 0,
    message: "demo",
    data: { user },
  });
  response.cookies.set(SESSION_COOKIE, "", { httpOnly: true, expires: new Date(0), path: "/" });
  response.cookies.set(DEMO_SESSION_COOKIE, audience, {
    httpOnly: true,
    sameSite: "lax",
    secure: process.env.NODE_ENV === "production",
    path: "/",
    maxAge: 60 * 60 * 8,
  });
  return response;
}
