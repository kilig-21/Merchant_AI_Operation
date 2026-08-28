import { DEMO_SESSION_COOKIE, SESSION_COOKIE } from "@/lib/session";
import { NextResponse } from "next/server";

export async function POST() {
  const response = NextResponse.json({ code: 0, message: "ok", data: null });
  response.cookies.set(SESSION_COOKIE, "", { httpOnly: true, expires: new Date(0), path: "/" });
  response.cookies.set(DEMO_SESSION_COOKIE, "", { httpOnly: true, expires: new Date(0), path: "/" });
  return response;
}
