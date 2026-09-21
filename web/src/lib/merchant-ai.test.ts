import { describe, expect, it } from "vitest";
import { MERCHANT_AI_MESSAGE_LIMIT, normalizeMerchantAiMessage } from "./merchant-ai";

describe("normalizeMerchantAiMessage", () => {
  it("trims a valid merchant question", () => {
    expect(normalizeMerchantAiMessage("  如何改善商品文案？  ")).toBe("如何改善商品文案？");
  });

  it("rejects blank input", () => {
    expect(normalizeMerchantAiMessage("   ")).toBeNull();
  });

  it("rejects input beyond the backend contract", () => {
    expect(normalizeMerchantAiMessage("问".repeat(MERCHANT_AI_MESSAGE_LIMIT + 1))).toBeNull();
  });
});
