# Morrow 首页 Design QA

更新日期：2026-08-10

## 本轮范围

- 将广告轮播下方的三个选物入口拆分为三张独立 GlassSurface 卡片。
- 保留原有链接、文字层级和键盘焦点，不修改消费者业务路径。
- 桌面端使用三栏玻璃卡片，移动端改为单列玻璃卡片。

## 对照来源

- 原入口布局：`C:/Users/Lenovo/AppData/Local/Temp/codex-clipboard-6ece29a7-aea5-4748-be12-5dd3eda7489b.png`
- Glass Surface 参数参考：`C:/Users/Lenovo/AppData/Local/Temp/codex-clipboard-01f64edc-afad-488d-9264-3e0306b8b840.png`

## 实现截图

- 桌面端：`qa-artifacts/campaign-glass-1440x900.png`
- 移动端：`qa-artifacts/campaign-glass-375x844.png`

## 浏览器实测

- 1440×900：三张卡片各自拥有独立边界、26px 圆角、玻璃位移滤镜、内高光和柔和阴影。
- 桌面端卡片高度为 280px，卡片之间保留 18px 呼吸距离，不再由一条共同外框连接。
- 375×844：卡片自动改为单列，最小高度 188px；标题、说明、分隔线和行动入口均无裁切或横向溢出。
- 三张卡片仍为完整可点击链接，语义名称和跳转地址保持正确。
- Chromium 使用 SVG backdrop displacement；不支持该能力的浏览器继续使用相同尺寸的半透明模糊降级表面。
- 页面控制台无 error 或 warning。

## 同视口对照结论

- 参考中的连续三栏已改为用户要求的三个独立方框，分组关系更清楚。
- 圆角控制在 26px，保持柔和弧度但没有变成胶囊。
- 玻璃层保持低透明、低色差和克制阴影，符合现有 Morrow 白色编辑型页面，不影响文字可读性。
- 卡片大小、内边距和原文案层级保持稳定，没有破坏广告章节的纵向节奏。

## 自动检查

- `npm run check`：通过。
- `npm run test`：通过，1 个测试文件、1 个测试用例。
- `npm run build`：通过，Next.js 生产构建与全部路由生成成功。
- 构建只出现 `baseline-browser-mapping` 数据版本提示，不影响编译和运行。

## 2026-08-10 广告章节滚动性能优化

- MorphSlider 改为按需渲染：静止时只保留最后一帧，只有切换动画进行时才连续绘制。
- WebGL 画布关闭抗锯齿并将 DPR 固定为 1；1280×720 视口下画布由约 1914×1106（约 211 万像素）降至 1276×737（约 94 万像素），像素处理量降低约 55%。
- 三张本期选物入口卡改用轻量 frosted 玻璃表面，不再为每张卡创建 SVG displacement filter；顶部主导航继续保留完整玻璃效果。
- 1440×900 实测画布为 1240×576，页面无横向溢出；375×844 实测画布为 328×540，三张入口卡正常单列显示。
- 广告左右按钮、指示器、自动播放、SplitText 文案和融化切换效果保持可用；优化没有改变商品跳转与后端请求。
- 浏览器控制台无 error 或 warning。

### 性能对照截图

- 优化前：`qa-artifacts/campaign-glass-1440x900.png`
- 优化后整体：`qa-artifacts/campaign-performance-optimized-1440x900.png`
- 优化后卡片：`qa-artifacts/campaign-performance-optimized-cards-1440x900.png`
- 优化后 1280×720：`qa-artifacts/campaign-performance-optimized-1280x720.png`

### 本轮自动检查

- `npm run check`：通过，Biome 与 TypeScript 均无错误。
- `npm run test`：通过，1 个测试文件、1 个测试用例全部通过。
- `npm run build`：通过，Next.js 生产构建及全部路由生成成功。
- 构建仅提示 `baseline-browser-mapping` 数据版本较旧，不影响编译或运行。

## 2026-08-10 横向轨道与叠页黑边修复

- “刚刚抵达”的三条横向商品轨道移除媒体卡片外投影，保留更浅的实体边框与原有圆角。
- 商品卡 Hover 不再重新产生黑色阴影边；深色主题使用低对比度浅色边框。
- FlowArt 叠页移除黑色上沿投影，并使用与纸页背景一致的 3px 无模糊遮罩覆盖旋转变换产生的抗锯齿灰边。
- 同视口对照确认：下一页进入时的斜向黑灰边已消失，页面仍保留原有旋转进入过程。
- 对照截图：`qa-artifacts/shadow-transition-fixed-1280x720.png`。
- 商品轨道截图：`qa-artifacts/shadow-edges-fixed-1280x720.png`。

## Final result

passed
