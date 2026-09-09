export const MERCHANT_AI_MESSAGE_LIMIT = 1000;

export const merchantAiStarterQuestions = [
  "帮我写一段新品上架公告",
  "给我三条通用的店铺经营建议",
  "如何更清晰地回复顾客售后问题？",
] as const;

/** 提交前统一去除首尾空白，并拒绝空内容或超长内容。 */
export function normalizeMerchantAiMessage(input: string) {
  const message = input.trim();
  if (!message || message.length > MERCHANT_AI_MESSAGE_LIMIT) return null;
  return message;
}
