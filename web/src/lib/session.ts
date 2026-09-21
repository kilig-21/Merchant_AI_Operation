import { cookies } from "next/headers";
import { getCurrentUserWithToken } from "./backend";
import { ApiError, type SessionUser } from "./types";

export const SESSION_COOKIE = "morrow_session";
export const DEMO_SESSION_COOKIE = "morrow_demo_role";

export type SessionState =
  | { status: "anonymous"; user: null }
  | { status: "unavailable"; user: null }
  | { status: "authenticated"; user: SessionUser };

export async function getSessionState(): Promise<SessionState> {
  const cookieStore = await cookies();
  const token = cookieStore.get(SESSION_COOKIE)?.value;
  const demoRole = cookieStore.get(DEMO_SESSION_COOKIE)?.value;
  if (!token && (demoRole === "consumer" || demoRole === "merchant")) {
    return {
      status: "authenticated",
      user:
        demoRole === "merchant"
          ? { id: 99002, username: "演示店主", userType: "MERCHANT_ADMIN", tenantId: 1001, isDemo: true }
          : { id: 99001, username: "演示会员", userType: "CONSUMER", tenantId: null, isDemo: true },
    };
  }
  if (!token) return { status: "anonymous", user: null };
  try {
    return { status: "authenticated", user: { ...(await getCurrentUserWithToken(token)), isDemo: false } };
  } catch (error) {
    if (error instanceof ApiError && error.status === 503) return { status: "unavailable", user: null };
    return { status: "anonymous", user: null };
  }
}
