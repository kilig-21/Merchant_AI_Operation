import { BoomerangVideoBg } from "@/components/BoomerangVideoBg";
import { HomeStoryExperience } from "@/components/HomeStoryExperience";
import { HorizontalProductRail } from "@/components/HorizontalProductRail";
import { ProductCard } from "@/components/ProductCard";
import { SiteFooter } from "@/components/SiteFooter";
import DepthText from "@/components/ui/DepthText";
import DriftWall from "@/components/ui/DriftWall";
import GlassSurface from "@/components/ui/GlassSurface";
import MorphSlider, { type MorphSlide } from "@/components/ui/MorphSlider";
import { FlowSection } from "@/components/ui/story-scroll";
import { demoProducts, heroPoster, productVisuals } from "@/lib/demo-data";
import { getJournalEntries } from "@/lib/journal";
import { RevealFx } from "@once-ui-system/core";
import Image from "next/image";
import Link from "next/link";

const primaryCurationTitles = ["为专注", "为停留", "为出发"];
const secondaryCurationTitles = ["为呼吸", "为轻装", "为松弛"];

const driftWallItems = [...productVisuals, ...productVisuals].map((item, index) => {
  const product = demoProducts[index % demoProducts.length];
  return {
    image: item.image,
    title: `${product.name} · ${item.category}`,
    href: `/stores/1001/products/${product.id}`,
  };
});

const arrivalShelves = [
  {
    eyebrow: "01 / NEW THIS WEEK",
    title: "本周新到",
    description: "刚刚完成上架的日常物件，库存与价格都已整理清楚。",
    products: demoProducts,
  },
  {
    eyebrow: "02 / QUIET WORK",
    title: "为安静工作准备",
    description: "从声音、光线到手边的温度，为需要投入的时刻减少干扰。",
    products: [demoProducts[0], demoProducts[1], demoProducts[3], demoProducts[2]],
  },
  {
    eyebrow: "03 / LIGHT TRAVEL",
    title: "轻装出发",
    description: "少带一点，但每一件都可靠；给通勤、短途与周末留出余量。",
    products: [demoProducts[4], demoProducts[2], demoProducts[5], demoProducts[3]],
  },
];

const campaignSlides: MorphSlide[] = [
  {
    image: "/media/campaign/prompt-01.webp",
    alt: "在旋转镜头中轻轻摇曳的白色野花",
    eyebrow: "MORROW / LISTEN 01",
    titleLines: ["安静，也可以", "很有力量。"],
    summary: "把专注留给自己，让声音落在刚刚好的地方。",
    href: "/stores",
    ctaLabel: "查看本期选物",
  },
  {
    image: "/media/campaign/prompt-02.webp",
    alt: "蓝紫色花朵在柔和光线中摇曳",
    eyebrow: "MORROW / GLOW 02",
    titleLines: ["给夜晚留一盏", "刚刚好的光。"],
    summary: "不追赶明亮，只为晚归与阅读留一小块温度。",
    href: "/stores",
    ctaLabel: "查看本期选物",
  },
  {
    image: "/media/campaign/prompt-03.webp",
    alt: "蓝色花朵在流动的绿色背景中掠过",
    eyebrow: "MORROW / TRAVEL 03",
    titleLines: ["少带一点，也能", "走得更远。"],
    summary: "轻装出发，只留下真正会被使用的可靠物件。",
    href: "/stores",
    ctaLabel: "查看本期选物",
  },
];

const campaignEntries = [
  {
    eyebrow: "01 / LISTEN",
    title: "把声音放回刚刚好的位置",
    meta: "耳机与桌面声音",
  },
  {
    eyebrow: "02 / GLOW",
    title: "给晚归与阅读留一小块光",
    meta: "桌面与氛围照明",
  },
  {
    eyebrow: "03 / TRAVEL",
    title: "只带真正会被使用的物件",
    meta: "通勤与短途出发",
  },
] as const;

export default function HomePage() {
  const entries = getJournalEntries();
  return (
    <>
      <HomeStoryExperience>
        <FlowSection
          aria-label="Morrow 本期首页"
          innerClassName="home-hero"
          style={{ backgroundColor: "#f8f8f5" }}
        >
          <BoomerangVideoBg poster={heroPoster} />
          <div className="hero-wash" />
          <div className="hero-layout">
            <div className="hero-index">
              <span>MORROW / ISSUE 01</span>
              <span>
                OBJECTS FOR
                <br />
                EVERYDAY LIFE
              </span>
            </div>
            <div className="hero-copy">
              <span className="eyebrow" style={{ color: "#171815" }}>
                THE QUIET EDIT / 2026
              </span>
              <h1>
                <DepthText
                  autoOrbit={false}
                  className="hero-depth-title"
                  depth={1.35}
                  depthColor="#66734f"
                  faceColor="#171815"
                  fontSize="inherit"
                  fontWeight={400}
                  layers={26}
                  perspective={1200}
                  shadow={false}
                  smoothing={0.12}
                  text="把喜欢的日常，"
                  tilt={4.5}
                />
                <DepthText
                  autoOrbit={false}
                  className="hero-depth-title"
                  depth={1.35}
                  depthColor="#66734f"
                  faceColor="#171815"
                  fontSize="inherit"
                  fontWeight={400}
                  layers={26}
                  perspective={1200}
                  shadow={false}
                  smoothing={0.12}
                  text="留在明天之前。"
                  tilt={4.5}
                />
              </h1>
              <p>我们挑选真正耐用、愿意每天使用的物件。少一点仓促，多一点刚刚好的决定。</p>
              <div className="hero-actions">
                <Link className="button dark" href="/stores">
                  开始选购 ↗
                </Link>
                <Link className="button" href="#curation">
                  查看本期策选
                </Link>
              </div>
              <aside className="hero-panel">
                <div className="hero-panel-top">
                  <div>
                    <span className="eyebrow">HOW WE CHOOSE</span>
                    <h2>
                      让每一次选择，
                      <br />
                      都有清楚依据。
                    </h2>
                  </div>
                  <p>从真实日常出发，提供清楚的商品、库存与订单信息，让喜欢不必依赖仓促决定。</p>
                </div>
                <div className="hero-panel-links">
                  <Link href="#curation">
                    01 / 精心策选 <b>→</b>
                  </Link>
                  <Link href="/stores">
                    02 / 信息清楚 <b>→</b>
                  </Link>
                  <Link href="/consumer/login">
                    03 / 安心下单 <b>→</b>
                  </Link>
                </div>
              </aside>
            </div>
          </div>
        </FlowSection>

        <FlowSection id="campaign" aria-label="Morrow 本期广告" innerClassName="campaign-story-panel">
          <div className="home-section campaign-story-section">
            <header className="section-head campaign-story-head">
              <div>
                <span className="eyebrow">MORROW / CAMPAIGN 01</span>
                <h2>让值得留下的物件，先和你见面。</h2>
              </div>
              <p>一组给日常留出余量的本期影像。慢一点经过，也能看见物件与生活之间的呼吸。</p>
            </header>
            <MorphSlider
              items={campaignSlides}
              transition="melt"
              intensity={0.42}
              aberration={0.18}
              drift={0.25}
              duration={1.15}
              autoplay
              autoplayDelay={4.8}
              loop
              radius={26}
            />
            <nav className="campaign-entry-grid" aria-label="本期选物入口">
              {campaignEntries.map((entry) => (
                <GlassSurface
                  key={entry.eyebrow}
                  width="100%"
                  height="100%"
                  borderRadius={26}
                  borderWidth={0.065}
                  brightness={94}
                  opacity={0.9}
                  blur={9}
                  displace={0.35}
                  backgroundOpacity={0.18}
                  saturation={1.08}
                  distortionScale={-68}
                  greenOffset={3}
                  blueOffset={6}
                  variant="frosted"
                  className="campaign-entry-glass"
                >
                  <Link
                    className="campaign-entry"
                    href="/stores"
                    aria-label={`${entry.title}，进入本期选物`}
                  >
                    <span className="eyebrow">{entry.eyebrow}</span>
                    <div>
                      <h3>{entry.title}</h3>
                      <p>{entry.meta}</p>
                    </div>
                    <strong>
                      进入本期选物 <b>↗</b>
                    </strong>
                  </Link>
                </GlassSurface>
              ))}
            </nav>
          </div>
        </FlowSection>

        <FlowSection id="curation" aria-label="按生活策选" innerClassName="story-paper-panel">
          <div className="home-section">
            <RevealFx translateY="8">
              <header className="section-head">
                <h2>
                  按生活发生的方式，
                  <br />
                  而不是按货架排列。
                </h2>
                <p>声音、光线、出发与停留。每一次策选都从真实场景开始，而不是从更多商品开始。</p>
              </header>
            </RevealFx>
            <div className="curation-grid">
              {productVisuals.slice(0, 3).map((item, index) => (
                <Link className="curation-card" href="/stores" key={item.id}>
                  <Image src={item.image} alt={item.category} fill sizes={index === 0 ? "60vw" : "40vw"} />
                  <div>
                    <span className="eyebrow">0{index + 1} / CURATION</span>
                    <h3>{primaryCurationTitles[index]}</h3>
                    <p>{item.tagline}</p>
                  </div>
                </Link>
              ))}
            </div>
            <div className="curation-chapter">
              <div>
                <span className="eyebrow">SCENES / 04—06</span>
                <h3>再往日常里走一点。</h3>
              </div>
              <p>空间需要呼吸，行李需要余量，周末也需要不被打扰的片刻。继续从场景出发，而不是从品类出发。</p>
            </div>
            <div className="curation-grid curation-grid-secondary">
              {productVisuals.slice(3).map((item, index) => (
                <Link className="curation-card" href="/stores" key={item.id}>
                  <Image src={item.image} alt={item.category} fill sizes="(max-width: 720px) 92vw, 33vw" />
                  <div>
                    <span className="eyebrow">0{index + 4} / CURATION</span>
                    <h3>{secondaryCurationTitles[index]}</h3>
                    <p>{item.tagline}</p>
                  </div>
                </Link>
              ))}
            </div>
          </div>
        </FlowSection>

        <FlowSection
          id="object-wall"
          aria-label="Morrow 漂移物件墙"
          innerClassName="story-paper-panel drift-story-panel"
        >
          <div className="home-section drift-story-section">
            <header className="section-head">
              <h2>
                物件在移动，
                <br />
                选择不必着急。
              </h2>
              <p>把本期物件放进一面持续漂移的墙。靠近时看清细节，离开时让它继续保持自己的节奏。</p>
            </header>
            <div className="drift-wall-frame">
              <div className="drift-wall-label">
                <span>MORROW / OBJECT WALL 01</span>
                <span>MOVE TO DISCOVER</span>
              </div>
              <DriftWall
                columns={5}
                depth={90}
                dim={0.72}
                fade={0.3}
                gap={16}
                grayscale={false}
                items={driftWallItems}
                lift={50}
                overlayColor="#314315"
                parallax={0.5}
                perspective={1400}
                radius={18}
                speed={24}
                tileHeight={150}
                tileWidth={210}
                tilt={12}
                turn={-13}
                variance={0.36}
              />
            </div>
            <div className="drift-story-notes">
              <article>
                <span className="eyebrow">01 / DRIFT</span>
                <h3>让浏览保持流动</h3>
                <p>不同方向与速度的移动，让相同的物件在每次经过时呈现新的组合。</p>
              </article>
              <article>
                <span className="eyebrow">02 / FOCUS</span>
                <h3>靠近一件，再看清它</h3>
                <p>鼠标经过或键盘聚焦时，卡片会从墙面轻轻抬起，恢复完整色彩。</p>
              </article>
              <article>
                <span className="eyebrow">03 / CONTINUE</span>
                <h3>墙在动，页面照常向下</h3>
                <p>物件墙拥有自己的动画节奏，不会阻断这一页内部和页面之间的纵向滚动。</p>
              </article>
            </div>
          </div>
        </FlowSection>

        <FlowSection id="arrivals" aria-label="刚刚抵达" innerClassName="arrival-band">
          <header className="section-head">
            <h2>刚刚抵达</h2>
            <p>每周少量更新。不是为了追赶新鲜，而是把值得留下的东西带到你面前。</p>
          </header>
          <div className="arrival-shelves">
            {arrivalShelves.map((shelf) => (
              <section className="arrival-shelf" key={shelf.eyebrow}>
                <div className="arrival-shelf-head">
                  <div>
                    <span className="eyebrow">{shelf.eyebrow}</span>
                    <h3>{shelf.title}</h3>
                  </div>
                  <p>{shelf.description}</p>
                </div>
                <HorizontalProductRail label={shelf.title}>
                  {shelf.products.map((product) => (
                    <ProductCard key={product.id} product={product} />
                  ))}
                </HorizontalProductRail>
              </section>
            ))}
          </div>
        </FlowSection>

        <FlowSection id="journal" aria-label="Morrow 选物志" innerClassName="story-paper-panel">
          <div className="home-section">
            <header className="section-head">
              <h2>选物志</h2>
              <p>关于物件、空间和选择的慢一点的内容。未来的品牌合作也会在这里明确标注。</p>
            </header>
            <div className="journal-tease">
              {entries.map((entry) => (
                <Link key={entry.slug} href={`/journal/${entry.slug}`} className="journal-feature">
                  <Image src={entry.cover} alt={entry.title} fill sizes="50vw" />
                  <div>
                    <span className="eyebrow">JOURNAL / {entry.publishedAt}</span>
                    <h3>{entry.title}</h3>
                    <p>{entry.summary}</p>
                  </div>
                </Link>
              ))}
            </div>
          </div>
        </FlowSection>
      </HomeStoryExperience>
      <SiteFooter />
    </>
  );
}
