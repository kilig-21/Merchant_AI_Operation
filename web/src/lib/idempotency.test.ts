import { describe, expect, it } from "vitest";
import { checkoutKey } from "./idempotency";

describe("checkoutKey", () => {
  it("is stable regardless of cart item order", () => {
    expect(checkoutKey([9, 2, 4])).toBe(checkoutKey([4, 9, 2]));
  });
});
