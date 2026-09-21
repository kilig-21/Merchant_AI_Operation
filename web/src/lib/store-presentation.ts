import { visualFor } from "./demo-data";

/** 仅提供本地色彩和插画，不包含任何店铺业务字段。 */
export function storePresentationFor(storeId: number) {
  const visual = visualFor(storeId);
  return { image: visual.image, tone: visual.tone, label: `STORE / ${storeId}` };
}
