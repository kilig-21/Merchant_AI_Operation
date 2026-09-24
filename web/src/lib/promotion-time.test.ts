import { describe, expect, it } from "vitest";
import { promotionPhase } from "./promotion-time";
import type { PublicPromotionActivity } from "./types";

const activity = {
  activityId: 1,
  activityItemId: 1,
  name: "限量活动",
  productName: "商品",
  skuName: "默认",
  activityPrice: 10,
  limitPerUser: 1,
  status: "SCHEDULED",
  stockStatus: "AVAILABLE",
  startAt: "2026-09-24T12:10:00Z",
  endAt: "2026-09-24T13:00:00Z",
} as PublicPromotionActivity;

describe("promotionPhase", () => {
  it("advances the countdown using the captured server clock offset", () => {
    const receivedAt = Date.parse("2026-09-24T12:00:00Z");
    expect(promotionPhase(activity, "2026-09-24T12:00:00Z", receivedAt, receivedAt)).toBe("10 分钟后开始");
    expect(promotionPhase(activity, "2026-09-24T12:00:00Z", receivedAt, receivedAt + 9 * 60_000)).toBe("1 分钟后开始");
    expect(promotionPhase(activity, "2026-09-24T12:00:00Z", receivedAt, receivedAt + 10 * 60_000)).toBe("等待服务端更新状态");
  });
});
