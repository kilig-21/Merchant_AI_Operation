import { getSessionState } from "@/lib/session";
import { NextResponse } from "next/server";

export async function GET() {
  const session = await getSessionState();
  if (session.status === "authenticated") {
    return NextResponse.json({ code: 0, message: "ok", data: session.user });
  }
  if (session.status === "anonymous") {
    return NextResponse.json({ code: 0, message: "anonymous", data: null });
  }
  const status = 503;
  return NextResponse.json(
    { code: status, message: "服务暂时无法连接。", data: null },
    { status },
  );
}
