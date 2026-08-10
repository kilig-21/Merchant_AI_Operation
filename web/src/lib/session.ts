import { cookies } from "next/headers";
import { getCurrentUserWithToken } from "./backend";
import { ApiError, type SessionUser } from "./types";

export const SESSION_COOKIE = "morrow_session";

export type SessionState =
  | { status: "anonymous"; user: null }
  | { status: "unavailable"; user: null }
  | { status: "authenticated"; user: SessionUser };

export async function getSessionState(): Promise<SessionState> {
  const token = (await cookies()).get(SESSION_COOKIE)?.value;
  if (!token) return { status: "anonymous", user: null };
  try {
    return { status: "authenticated", user: await getCurrentUserWithToken(token) };
  } catch (error) {
    if (error instanceof ApiError && error.status === 503) return { status: "unavailable", user: null };
    return { status: "anonymous", user: null };
  }
}
