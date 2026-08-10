import { getSessionState } from "@/lib/session";
import { NextResponse } from "next/server";

export async function GET() {
  const session = await getSessionState();
  if (session.status === "authenticated") {
    return NextResponse.json({ code: 0, message: "ok", data: session.user });
  }
  const status = session.status === "unavailable" ? 503 : 401;
  return NextResponse.json(
    { code: status, message: status === 503 ? "服务暂时无法连接。" : "尚未登录。", data: null },
    { status },
  );
}
