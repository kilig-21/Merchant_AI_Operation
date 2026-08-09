<script setup lang="ts">
import { computed, ref } from 'vue'
import ConsumerNav from '../components/ConsumerNav.vue'
import BoomerangVideoBg from '../components/BoomerangVideoBg.vue'
import ProductMedia from '../components/ProductMedia.vue'
import EditorialFooter from '../components/EditorialFooter.vue'
import { currency, demoProducts, heroMedia, journalMedia } from '../data/consumerCatalog'
import { usePointerPosition } from '../composables/usePointerPosition'
import { useReveal } from '../composables/useReveal'

const hero = ref<HTMLElement | null>(null)
const categories = computed(() => [
  { label: '为专注', sub: '声音、秩序与不被打扰', image: demoProducts[0].image },
  { label: '为停留', sub: '光线、气味与舒适尺度', image: demoProducts[3].image },
  { label: '为出发', sub: '轻量、可靠与随身携带', image: demoProducts[4].image },
])

usePointerPosition({ container: hero })
useReveal()
</script>

<template>
  <div class="home-view">
    <ConsumerNav />
    <main>
      <section ref="hero" class="hero">
        <BoomerangVideoBg :poster="heroMedia" />
        <div class="hero__wash" aria-hidden="true" />
        <div class="hero__grid">
          <div class="hero__index">
            <p class="meta-label">MORROW / ISSUE 01</p>
            <span>Objects for<br />everyday life</span>
          </div>
          <div class="hero__copy">
            <p class="meta-label">THE QUIET EDIT / 2026</p>
            <h1><span>把喜欢的日常，</span><span>留在明天之前。</span></h1>
            <p>我们挑选真正耐用、愿意每天使用的物件。少一点仓促，多一点刚刚好的决定。</p>
            <div class="hero__actions">
              <RouterLink class="hero__primary" to="/stores/1001/products">开始选购 <b>↗</b></RouterLink>
              <a href="#curation">查看本期策选</a>
            </div>
            <aside class="hero__info-panel" aria-label="Morrow 选购说明">
              <div class="hero__info-intro">
                <div>
                  <p class="meta-label">HOW WE CHOOSE</p>
                  <h2>让每一次选择，<br />都有清楚依据。</h2>
                </div>
                <p>从真实日常出发，提供清楚的商品、库存与订单信息，让喜欢不必依赖仓促决定。</p>
              </div>
              <div class="hero__feature-links">
                <a href="#curation"><span>01 /</span> 精心策选 <b>→</b></a>
                <RouterLink to="/stores/1001/products"><span>02 /</span> 信息清楚 <b>→</b></RouterLink>
                <RouterLink to="/consumer/login"><span>03 /</span> 安心下单 <b>→</b></RouterLink>
              </div>
            </aside>
          </div>
          <div class="hero__coordinates" aria-hidden="true">X <span>048</span> / Y <span>026</span></div>
        </div>
      </section>

      <section class="manifesto" data-reveal>
        <p class="meta-label">A BETTER WAY TO CHOOSE</p>
        <div>
          <h2>不是更多，<br />是更接近你。</h2>
          <p>电商不必一直催促。Morrow 把商品、信息与服务放回清楚的位置，让浏览有呼吸，决定有依据。</p>
        </div>
      </section>

      <section id="curation" class="curation">
        <header data-reveal>
          <p class="meta-label">CURATED FOR A REASON</p>
          <h2>按生活发生的方式，<br />而不是按货架排列。</h2>
          <RouterLink to="/stores/1001/products">全部商品 ↗</RouterLink>
        </header>
        <div class="curation__grid">
          <RouterLink v-for="(item, index) in categories" :key="item.label" to="/stores/1001/products" class="curation-card" :class="`curation-card--${index + 1}`" data-reveal>
            <ProductMedia :src="item.image" :alt="item.label" :tone="demoProducts[index].tone" />
            <div>
              <span class="meta-label">0{{ index + 1 }}</span>
              <h3>{{ item.label }}</h3>
              <p>{{ item.sub }}</p>
              <b aria-hidden="true">↗</b>
            </div>
          </RouterLink>
        </div>
      </section>

      <section class="arrival-section">
        <header data-reveal>
          <div><p class="meta-label">NEW / RECENTLY ADDED</p><h2>刚刚抵达</h2></div>
          <p>每周少量更新。不是为了追赶新鲜，而是把值得留下的东西带到你面前。</p>
        </header>
        <div class="arrival-rail" aria-label="新品横向列表">
          <RouterLink v-for="(item, index) in demoProducts" :key="item.id" :to="`/stores/1001/products/${item.id}`" class="arrival-card">
            <ProductMedia :src="item.image" :alt="item.imageAlt" :tone="item.tone" />
            <div class="arrival-card__meta">
              <span class="meta-label">{{ String(index + 1).padStart(2, '0') }} / {{ item.category }}</span>
              <h3>{{ item.name }}</h3>
              <p>{{ item.tagline }}</p>
              <strong>{{ currency(item.minSalePrice) }}</strong>
            </div>
          </RouterLink>
        </div>
      </section>

      <section class="slow-story" data-reveal>
        <ProductMedia :src="journalMedia" alt="自然光中的安静工作空间" tone="#d9ded4" />
        <div class="slow-story__copy">
          <p class="meta-label">MARGINALIA / 001</p>
          <h2>给空间一点<br /><em>慢下来</em>的理由。</h2>
          <p>好物不是把房间堆满，而是让每一处都更像自己。安静、耐用，并且愿意一次又一次地拿起来使用。</p>
          <RouterLink to="/stores/1001/products">探索空间精选 <span>↗</span></RouterLink>
        </div>
      </section>

      <section class="service-ledger">
        <article data-reveal><span class="meta-label">01 / SELECT</span><h3>细心挑选</h3><p>从真实的日常需求出发，不用概念替代体验。</p></article>
        <article data-reveal><span class="meta-label">02 / CLEAR</span><h3>信息清楚</h3><p>价格、库存和订单状态，都放在你需要看到的位置。</p></article>
        <article data-reveal><span class="meta-label">03 / SAFE</span><h3>安心决定</h3><p>每次下单都重新确认价格与库存，把选择权留给你。</p></article>
      </section>
    </main>
    <EditorialFooter />
  </div>
</template>

<style scoped>
.home-view { background: var(--paper); }
.home-view :deep(.consumer-nav-wrap) { position: fixed; width: 100%; border-color: rgba(23,24,21,.12); background: rgba(247,245,239,.15); }
.hero { position: relative; min-height: 100svh; overflow: hidden; background: var(--paper); }
.hero :deep(.boomerang-video) { inset: 0; transform: scale(1.15); filter: saturate(.78) contrast(.94); }
.hero__wash { position: absolute; z-index: 1; inset: 0; pointer-events: none; background: linear-gradient(90deg, rgba(247,245,239,.5) 0%, rgba(247,245,239,.08) 42%, rgba(247,245,239,.28) 66%, rgba(247,245,239,.78) 100%); }
.hero__wash::after { position: absolute; inset: 0; content: ''; opacity: .65; background: radial-gradient(440px circle at var(--pointer-x, 70%) var(--pointer-y, 45%), rgba(198,240,77,.18), transparent 62%); mix-blend-mode: screen; }
.hero__grid { position: relative; z-index: 2; display: grid; grid-template-columns: repeat(12, 1fr); grid-template-rows: auto 1fr auto; width: var(--page); min-height: 100svh; margin: auto; padding: 96px 0 34px; }
.hero__index { position: relative; grid-column: 1 / span 4; min-height: 150px; }
.hero__index .meta-label { align-self: flex-start; margin-right: 0; }
.hero__index > span { position: absolute; top: 0; left: 65%; margin: 0; }
.hero__index span { color: var(--ink-soft); font-family: var(--mono); font-size: 10px; line-height: 1.55; text-transform: uppercase; }
.hero__copy { grid-column: 7 / -1; grid-row: 1 / -1; align-self: center; justify-self: end; width: min(100%, 720px); padding: 72px 0 0; transform: translateY(-48px); }
.hero__copy > .meta-label { margin-bottom: 18px; }
.hero h1 { margin: 0; font-family: "Songti SC", "STSong", "SimSun", Georgia, serif; font-size: clamp(52px, 4.7vw, 84px); font-weight: 400; letter-spacing: -.065em; line-height: .98; }
.hero h1 span { display: block; white-space: nowrap; }
.hero__copy > p:not(.meta-label) { max-width: 500px; margin: 22px 0 0; color: var(--ink-soft); font-size: 14px; line-height: 1.65; }
.hero__actions { display: flex; align-items: center; gap: 24px; margin-top: 24px; }
.hero__actions a { min-height: 48px; padding: 0 6px; font-family: var(--mono); font-size: 11px; line-height: 48px; text-transform: uppercase; }
.hero__actions .hero__primary { display: flex; align-items: center; justify-content: space-between; min-width: 180px; padding: 0 16px; color: var(--paper); background: var(--ink); line-height: 1; }
.hero__primary b { color: var(--signal); font-size: 14px; }
.hero__info-panel { margin-top: 28px; color: var(--ink); border: 1px solid rgba(23,24,21,.13); background: rgba(255,255,255,.84); box-shadow: 0 12px 45px rgba(23,24,21,.06); backdrop-filter: blur(10px); }
.hero__info-intro { display: grid; grid-template-columns: .92fr 1.08fr; align-items: end; gap: 30px; padding: 25px 28px 22px; }
.hero__info-intro .meta-label { margin: 0 0 10px; color: var(--ink-faint); font-size: 8px; }
.hero__info-intro h2 { margin: 0; font-family: "Songti SC", "STSong", "SimSun", Georgia, serif; font-size: 25px; font-weight: 400; letter-spacing: -.035em; line-height: 1.12; }
.hero__info-intro > p { margin: 0; color: var(--ink-soft); font-size: 11px; line-height: 1.65; }
.hero__feature-links { display: grid; grid-template-columns: repeat(3, 1fr); gap: 3px; padding: 3px; border-top: 1px solid var(--line); }
.hero__feature-links a { display: grid; grid-template-columns: auto 1fr auto; align-items: center; min-height: 48px; padding: 0 13px; background: rgba(238,237,233,.9); font-size: 10px; transition: background-color .2s; }
.hero__feature-links a:hover { background: #e5e4de; }
.hero__feature-links span { margin-right: 7px; color: var(--ink-faint); font-family: var(--mono); font-size: 8px; }
.hero__feature-links b { color: var(--ink-faint); font-size: 12px; font-weight: 400; transition: transform .2s, color .2s; }
.hero__feature-links a:hover b { color: var(--ink); transform: translateX(3px); }
.hero__coordinates { grid-column: 11 / span 2; grid-row: 3; justify-self: end; font-family: var(--mono); font-size: 9px; letter-spacing: .08em; }
.hero__coordinates span { color: var(--ink-soft); }

.manifesto { display: grid; grid-template-columns: 3fr 9fr; width: var(--page); margin: auto; padding: 150px 0 190px; }
.manifesto > .meta-label { padding-top: 13px; color: var(--ink-soft); }
.manifesto > div { grid-column: 5 / -1; }
.manifesto h2 { margin: 0; font-family: var(--display); font-size: clamp(54px, 7vw, 105px); font-weight: 500; letter-spacing: -.065em; line-height: .9; }
.manifesto > div p { max-width: 560px; margin: 42px 0 0 auto; color: var(--ink-soft); font-size: 17px; line-height: 1.7; }

.curation { padding: 0 0 170px; }
.curation > header { display: grid; grid-template-columns: 3fr 7fr 2fr; align-items: end; width: var(--page); margin: 0 auto 55px; }
.curation header .meta-label { align-self: start; padding-top: 12px; color: var(--ink-soft); }
.curation header h2 { margin: 0; font-family: var(--display); font-size: clamp(42px, 5vw, 76px); font-weight: 500; letter-spacing: -.055em; line-height: .96; }
.curation header a { justify-self: end; padding-bottom: 6px; font-family: var(--mono); font-size: 10px; text-transform: uppercase; }
.curation__grid { display: grid; grid-template-columns: 1.14fr .86fr; gap: 18px; width: min(1480px, calc(100% - 24px)); margin: auto; }
.curation-card { position: relative; display: block; }
.curation-card :deep(.product-media) { height: 100%; min-height: 520px; }
.curation-card--1 { grid-row: span 2; }
.curation-card--1 :deep(.product-media) { min-height: 1058px; }
.curation-card > div:last-child { position: absolute; z-index: 4; right: 0; bottom: 0; left: 0; display: grid; grid-template-columns: 42px 1fr auto; align-items: end; gap: 10px; padding: 25px; color: #fff; background: linear-gradient(transparent, rgba(10,12,10,.72)); }
.curation-card h3 { margin: 0; font-family: var(--display); font-size: clamp(32px, 4vw, 60px); font-weight: 500; letter-spacing: -.05em; }
.curation-card p { grid-column: 2; margin: 5px 0 0; color: rgba(255,255,255,.7); font-size: 13px; }
.curation-card b { grid-column: 3; grid-row: 1 / span 2; color: var(--signal); font-size: 21px; }

.arrival-section { padding: 145px 0 170px; background: var(--paper-raised); }
.arrival-section > header { display: grid; grid-template-columns: 1fr 1fr; align-items: end; width: var(--page); margin: auto; }
.arrival-section h2 { margin: 13px 0 0; font-family: var(--display); font-size: clamp(52px, 6vw, 88px); font-weight: 500; letter-spacing: -.06em; line-height: .9; }
.arrival-section header > p { justify-self: end; max-width: 420px; margin: 0; color: var(--ink-soft); font-size: 15px; line-height: 1.65; }
.arrival-rail { display: grid; grid-auto-columns: minmax(300px, 31vw); grid-auto-flow: column; gap: 14px; margin-top: 60px; padding: 0 max(32px, calc((100vw - 1320px) / 2)) 28px; overflow-x: auto; scroll-snap-type: x mandatory; scrollbar-color: var(--ink) transparent; scrollbar-width: thin; }
.arrival-card { scroll-snap-align: start; }
.arrival-card :deep(.product-media) { aspect-ratio: .82; }
.arrival-card__meta { position: relative; padding: 18px 0 0; }
.arrival-card h3 { margin: 10px 0 0; font-family: var(--display); font-size: 26px; font-weight: 600; letter-spacing: -.03em; }
.arrival-card p { margin: 6px 0 0; color: var(--ink-soft); font-size: 13px; }
.arrival-card strong { position: absolute; top: 18px; right: 0; font-family: var(--mono); font-size: 11px; font-weight: 500; }

.slow-story { display: grid; grid-template-columns: 1.15fr .85fr; min-height: 760px; }
.slow-story :deep(.product-media) { min-height: 760px; }
.slow-story__copy { display: flex; flex-direction: column; align-items: flex-start; justify-content: center; padding: clamp(50px, 8vw, 130px); background: var(--paper-warm); }
.slow-story h2 { margin: 28px 0 36px; font-family: var(--display); font-size: clamp(48px, 5vw, 82px); font-weight: 500; letter-spacing: -.06em; line-height: .92; }
.slow-story h2 em { font-family: Georgia, serif; font-weight: 400; }
.slow-story__copy > p:not(.meta-label) { max-width: 420px; margin: 0; color: var(--ink-soft); line-height: 1.7; }
.slow-story a { margin-top: 36px; padding-bottom: 5px; border-bottom: 1px solid var(--ink); font-family: var(--mono); font-size: 10px; text-transform: uppercase; }
.slow-story a span { margin-left: 12px; }

.service-ledger { display: grid; grid-template-columns: repeat(3, 1fr); width: var(--page); margin: auto; padding: 130px 0 150px; }
.service-ledger article { min-height: 220px; padding: 22px 28px 0 0; border-top: 1px solid var(--ink); }
.service-ledger article + article { padding-left: 28px; border-left: 1px solid var(--line); }
.service-ledger h3 { margin: 55px 0 12px; font-family: var(--display); font-size: 29px; letter-spacing: -.03em; }
.service-ledger p { max-width: 300px; margin: 0; color: var(--ink-soft); font-size: 13px; line-height: 1.6; }

@media (max-width: 900px) {
  .hero__wash { background: linear-gradient(90deg, rgba(247,245,239,.35), rgba(247,245,239,.78) 86%); }
  .hero__copy { grid-column: 5 / -1; transform: translateY(-28px); }
  .manifesto { grid-template-columns: 1fr; }
  .manifesto > div { grid-column: 1; margin-top: 50px; }
  .curation > header { grid-template-columns: 1fr; gap: 24px; }
  .curation header a { justify-self: start; }
  .curation__grid { grid-template-columns: 1fr 1fr; }
  .curation-card--1 { grid-column: 1 / -1; grid-row: auto; }
  .curation-card--1 :deep(.product-media) { min-height: 720px; }
  .slow-story { grid-template-columns: 1fr; }
  .slow-story :deep(.product-media) { min-height: 560px; }
}

@media (max-width: 640px) {
  .hero { min-height: 1040px; }
  .hero :deep(.boomerang-video) { inset: 0; transform: scale(1.15); opacity: .56; }
  .hero__wash { background: linear-gradient(180deg, rgba(247,245,239,.5), rgba(247,245,239,.92) 70%); }
  .hero__grid { min-height: 1040px; padding-top: 94px; }
  .hero__index { grid-column: 1 / span 8; min-height: 100px; }
  .hero__index > span { position: static; display: block; margin-top: 52px; }
  .hero__copy { grid-column: 1 / -1; grid-row: 2 / -1; align-self: end; justify-self: start; padding: 0 0 34px; transform: none; }
  .hero h1 { font-size: clamp(46px, 13vw, 61px); line-height: 1; }
  .hero h1 span { white-space: normal; }
  .hero__copy > p:not(.meta-label) { font-size: 14px; }
  .hero__coordinates { display: none; }
  .hero__actions { align-items: flex-start; flex-direction: column; gap: 4px; }
  .hero__info-panel { margin-top: 22px; }
  .hero__info-intro { grid-template-columns: 1fr; gap: 14px; padding: 22px 20px 18px; }
  .hero__info-intro h2 { font-size: 23px; }
  .hero__feature-links { grid-template-columns: 1fr; }
  .manifesto { padding: 100px 0 120px; }
  .manifesto h2 { font-size: 53px; }
  .manifesto > div p { margin-top: 28px; font-size: 15px; }
  .curation { padding-bottom: 100px; }
  .curation__grid { grid-template-columns: 1fr; width: calc(100% - 16px); }
  .curation-card--1 { grid-column: auto; }
  .curation-card :deep(.product-media), .curation-card--1 :deep(.product-media) { min-height: 520px; }
  .arrival-section { padding: 95px 0 105px; }
  .arrival-section > header { grid-template-columns: 1fr; gap: 25px; }
  .arrival-section header > p { justify-self: start; }
  .arrival-rail { grid-auto-columns: 82vw; margin-top: 40px; padding-inline: 16px; }
  .slow-story :deep(.product-media) { min-height: 440px; }
  .slow-story__copy { padding: 70px 16px 82px; }
  .slow-story h2 { font-size: 52px; }
  .service-ledger { grid-template-columns: 1fr; padding: 80px 0 100px; }
  .service-ledger article, .service-ledger article + article { min-height: 185px; padding: 22px 0 30px; border-left: 0; }
  .service-ledger h3 { margin-top: 40px; }
}

@media (pointer: coarse), (prefers-reduced-motion: reduce) { .hero__wash::after { display: none; } }
</style>
