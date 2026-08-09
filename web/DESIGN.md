# MORROW / Quiet Commerce

## 1. Visual Theme & Atmosphere

Morrow is a practical editorial storefront: calm enough to browse slowly, clear enough to buy without hesitation. Consumer pages feel like a contemporary object magazine; merchant pages feel like a focused operating desk.

- Utility / expression ratio: 75 / 25.
- Use asymmetry, typography and image rhythm for character—not decorative cards.
- Motion explains hierarchy and direction. It must never delay a transaction.
- Preserve the existing homepage `heroMedia` and `BoomerangVideoBg` mechanism.

## 2. Color Palette & Roles

| Token | Value | Role |
|---|---|---|
| Paper | `#f7f5ef` | Primary consumer canvas |
| Paper raised | `#eeebe2` | Secondary editorial surface |
| Ink | `#171815` | Primary type and dark actions |
| Ink soft | `rgba(23,24,21,.58)` | Supporting copy |
| Hairline | `rgba(23,24,21,.14)` | Dividers and controls |
| Signal | `#c6f04d` | Rare interaction accent, max ~3% |
| Merchant dark | `#181b19` | Merchant navigation |
| Error | `#9e3e2c` | Errors and destructive state |
| Success | `#2f6b4f` | Paid / available / success |

## 3. Typography

- Display: `Arial Narrow`, `Aptos Display`, `PingFang SC`, sans-serif.
- Body: `Inter`, `PingFang SC`, `Microsoft YaHei`, system UI.
- Metadata: `IBM Plex Mono`, `SFMono-Regular`, `Consolas`, monospace.
- Consumer display headings may reach `clamp(3rem, 7vw, 7.5rem)`.
- Merchant headings stay between 28px and 44px.
- Uppercase English eyebrows use 0.12–0.18em tracking.

## 4. Layout Principles

- Desktop: 12-column grid, max width 1440px, 24–32px gutters.
- Consumer content max width: 1320px.
- Merchant shell: 248px navigation + fluid workspace.
- Prefer thin dividers, full-width bands, and image-led composition over repeated boxes.
- Mobile reorders content by task priority rather than shrinking desktop geometry.

## 5. Components

- Buttons: square-to-soft corners (0–8px), minimum height 44px. Pills are reserved for filters and compact status.
- Cards: no shadow by default. Use image, whitespace and a single border to establish grouping.
- Inputs: paper surface, one-pixel border, visible focus ring.
- Status: small monospaced pill with semantic color.
- Demo data: always marked with a visible `DEMO / 演示` notice.

## 6. Motion

- UI feedback: 160–240ms.
- Editorial reveal: 650–900ms, translateY no more than 42px.
- Image hover: scale <= 1.025; pointer shift <= 6px; tilt <= 1.2deg.
- Homepage pointer glow is local, soft and non-interactive.
- Disable decorative motion for `prefers-reduced-motion`, coarse pointers and transactional surfaces.

## 7. Responsive Behavior

- Breakpoints: 1080px, 820px, 640px.
- Touch targets are at least 44px.
- Horizontal editorial rails use native overflow and scroll snap.
- Navigation collapses into a readable full-width drawer.
- Merchant tables may scroll horizontally; primary actions remain visible.

## 8. Do / Don't

Do:
- Keep the main shopping path obvious.
- Label static data honestly.
- Use real source photography and existing assets.
- Preserve visible keyboard focus and native scrolling on forms and operations pages.

Don't:
- Add global WebGL, autoplay sound or scroll hijacking.
- Use acid green as a large background.
- Turn every section into a rounded card.
- Fake backend-supported states or controls.
- Modify anything outside `web/`.

