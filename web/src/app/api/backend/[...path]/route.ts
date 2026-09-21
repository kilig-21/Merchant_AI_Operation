import { backendOrigin } from "@/lib/backend";
import { SESSION_COOKIE } from "@/lib/session";
import { cookies } from "next/headers";
import { NextResponse } from "next/server";

type Context = { params: Promise<{ path: string[] }> };

async function forward(request: Request, context: Context) {
  const { path } = await context.params;
  if (!path.length || path.some((part) => !/^[a-zA-Z0-9_-]+$/.test(part))) {
    return NextResponse.json({ code: 400, message: "无效的接口路径。", data: null }, { status: 400 });
  }

  const source = new URL(request.url);
  const target = new URL(`/api/${path.join("/")}${source.search}`, backendOrigin());
  const headers = new Headers();
  const contentType = request.headers.get("content-type");
  const idempotencyKey = request.headers.get("idempotency-key");
  if (contentType) headers.set("content-type", contentType);
  if (idempotencyKey) headers.set("idempotency-key", idempotencyKey);
  const token = (await cookies()).get(SESSION_COOKIE)?.value;
  if (token) headers.set("authorization", `Bearer ${token}`);

  try {
    const response = await fetch(target, {
      method: request.method,
      headers,
      body: request.method === "GET" || request.method === "HEAD" ? undefined : await request.arrayBuffer(),
      cache: "no-store",
    });
    const forwarded = new NextResponse(response.body, {
      status: response.status,
      headers: { "content-type": response.headers.get("content-type") || "application/json" },
    });
    if (response.status === 401) {
      forwarded.cookies.set(SESSION_COOKIE, "", { httpOnly: true, expires: new Date(0), path: "/" });
    }
    return forwarded;
  } catch {
    return NextResponse.json({ code: 503, message: "服务暂时无法连接。", data: null }, { status: 503 });
  }
}

export const GET = forward;
export const POST = forward;
export const PUT = forward;
export const PATCH = forward;
export const DELETE = forward;
