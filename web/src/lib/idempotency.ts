export function checkoutKey(cartItemIds: number[]) {
  return `morrow_checkout_key_${[...cartItemIds].sort((a, b) => a - b).join("_")}`;
}

export function getIdempotencyKey(cartItemIds: number[]) {
  const key = checkoutKey(cartItemIds);
  const value = sessionStorage.getItem(key) ?? crypto.randomUUID();
  sessionStorage.setItem(key, value);
  return value;
}
