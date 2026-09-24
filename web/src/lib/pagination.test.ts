import { describe, expect, it } from "vitest";
import { appendUniquePage, hasNextPage } from "./pagination";

describe("paged lists", () => {
  it("keeps existing rows and ignores duplicates across and within pages", () => {
    expect(appendUniquePage([{ id: 1 }], [{ id: 1 }, { id: 2 }, { id: 2 }], (item) => item.id)).toEqual([{ id: 1 }, { id: 2 }]);
  });

  it("supports a store-scoped product identity", () => {
    const current = [{ storeId: 1, id: 7 }];
    const next = [{ storeId: 2, id: 7 }];
    expect(appendUniquePage(current, next, (item) => `${item.storeId}-${item.id}`)).toEqual([...current, ...next]);
  });

  it("offers another page only after receiving a full page", () => {
    expect(hasNextPage(48, 48)).toBe(true);
    expect(hasNextPage(47, 48)).toBe(false);
    expect(hasNextPage(0, 48)).toBe(false);
  });
});
