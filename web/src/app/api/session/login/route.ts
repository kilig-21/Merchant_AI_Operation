import { backendRequest } from "@/lib/backend";
import { SESSION_COOKIE } from "@/lib/session";
import { type ApiEnvelope, ApiError, type LoginResult } from "@/lib/types";
import { NextResponse } from "next/server";

export async function POST(request: Request) {
  try {
    const credentials = await request.json();
    const result = await backendRequest<LoginResult>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify(credentials),
    });
    const response = NextResponse.json<ApiEnvelope<{ user: LoginResult["user"] }>>({
      code: 0,
      message: "ok",
      data: { user: result.user },
    });
    response.cookies.set(SESSION_COOKIE, result.accessToken, {
      httpOnly: true,
      sameSite: "lax",
      secure: process.env.NODE_ENV === "production",
      path: "/",
    });
    return response;
  } catch (error) {
    const caught = error instanceof ApiError ? error : new ApiError("登录失败。", 500);
    return NextResponse.json(
      { code: caught.status, message: caught.message, data: null },
      { status: caught.status },
    );
  }
}
