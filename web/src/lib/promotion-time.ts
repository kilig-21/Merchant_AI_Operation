import type { PublicPromotionActivity } from "./types";

export function promotionPhase(activity: PublicPromotionActivity, serverTime: string, receivedAt: number, current: number) {
  if (activity.status === "ACTIVE") return "正在进行";
  const serverNow = Date.parse(serverTime) + (current - receivedAt);
  const remaining = Date.parse(activity.startAt) - serverNow;
  if (remaining <= 0) return "等待服务端更新状态";
  return `${Math.ceil(remaining / 60_000)} 分钟后开始`;
}
