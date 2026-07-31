<script setup lang="ts">
import ConsumerNav from '../components/ConsumerNav.vue'
import BoomerangVideoBg from '../components/BoomerangVideoBg.vue'
import ProductMedia from '../components/ProductMedia.vue'
import { demoProducts, heroMedia, journalMedia, currency } from '../data/consumerCatalog'

const featured = demoProducts.slice(0, 4)
</script>

<template>
  <div class="home-view">
    <ConsumerNav />
    <main>
      <section class="hero">
        <BoomerangVideoBg :poster="heroMedia" />
        <div class="hero__copy">
          <p class="kicker">MORROW / 2026 SUMMER</p>
          <h1><span>为每一个</span><span>正在发生的日常。</span></h1>
          <p>把实用、愉悦与恰到好处的设计，带回你的生活里。</p>
          <RouterLink class="button button--light" to="/stores/1/products">开始选购 <span>→</span></RouterLink>
        </div>
        <section class="hero__bridge" aria-label="Morrow 精选理念">
          <div class="hero__bridge-top">
            <div>
              <p class="hero__micro">THE MORROW EDIT</p>
              <h2>让每一次选择，<br />都更接近你自己。</h2>
            </div>
            <p>从一件真正耐用、好看、愿意每天使用的物件开始。我们把复杂的挑选，留在了身后。</p>
          </div>
          <div class="hero__bridge-links">
            <RouterLink to="/stores/1/products"><span>01</span> 为专注 <b>→</b></RouterLink>
            <RouterLink to="/stores/1/products"><span>02</span> 为停留 <b>→</b></RouterLink>
            <RouterLink to="/stores/1/products"><span>03</span> 为出发 <b>→</b></RouterLink>
          </div>
        </section>
      </section>

      <section class="intro-band" id="new-arrivals">
        <p class="kicker">NEW ARRIVALS</p>
        <h2>新鲜抵达的物件，<br />为今天留一点新意。</h2>
        <RouterLink to="/stores/1/products">查看全部新品 <span>→</span></RouterLink>
      </section>

      <section class="featured-grid" aria-label="精选商品">
        <RouterLink v-for="(item, index) in featured" :key="item.id" :to="`/products/${item.id}`" class="feature-card" :class="`feature-card--${index}`">
          <ProductMedia :src="item.image" :alt="item.imageAlt" :tone="item.tone">
            <div class="feature-card__copy" :class="{ dark: index === 0 }">
              <p>{{ item.category }} <span v-if="item.isNew">新上架</span></p>
              <h3>{{ item.name }}</h3>
              <small>{{ item.tagline }}</small>
              <b>{{ currency(item.minSalePrice) }} <em>了解更多 →</em></b>
            </div>
          </ProductMedia>
        </RouterLink>
      </section>

      <section class="story-panel" id="journal">
        <div class="story-panel__copy">
          <p class="kicker">THE SLOW EDIT</p>
          <h2>给空间一点<br />慢下来的理由。</h2>
          <p>好物不是堆满房间，而是让每一处都更像自己。我们的选择，从安静、耐用和每天都想使用开始。</p>
          <RouterLink to="/stores/1/products" class="text-link">探索空间灵感 <span>→</span></RouterLink>
        </div>
        <ProductMedia :src="journalMedia" alt="简洁舒适的工作桌" tone="#d9ded4" />
      </section>

      <section class="service-strip">
        <div><strong>细心挑选</strong><span>每件商品都从日常出发</span></div>
        <div><strong>安心送达</strong><span>订单状态始终清楚可见</span></div>
        <div><strong>轻松退换</strong><span>把决定权留在你手中</span></div>
      </section>
    </main>
    <footer class="consumer-footer"><span>Morrow Store</span><span>让日常慢慢变好</span><RouterLink to="/merchant/login">商家入口</RouterLink></footer>
  </div>
</template>

<style scoped>
.home-view { background: #fff; }.home-view :deep(.consumer-nav-wrap) { position: fixed; width: 100%; background: transparent; backdrop-filter: none; -webkit-backdrop-filter: none; }.home-view :deep(.consumer-nav) { color: #191919; }.home-view :deep(.consumer-nav__links a), .home-view :deep(.consumer-nav__links a:hover), .home-view :deep(.consumer-nav__links a.active), .home-view :deep(.consumer-nav__links a.router-link-exact-active) { color: #191919; }
.hero { position: relative; display: flex; flex-direction: column; height: 100svh; min-height: 760px; overflow: hidden; color: #191919; background: #fff; }
.hero__copy { position: absolute; z-index: 2; top: clamp(155px, 20vh, 230px); left: clamp(48%, 54vw, 58%); width: min(520px, 35vw); margin: 0; padding: 0; pointer-events: none; }.hero__copy a { pointer-events: auto; }
.kicker { margin: 0; font-size: 12px; font-weight: 600; letter-spacing: .12em; }
.hero h1 { max-width: none; margin: 18px 0; font-family: Georgia, 'Times New Roman', serif; font-size: clamp(42px, 3.3vw, 64px); font-weight: 400; letter-spacing: 0; line-height: 1.08; }.hero h1 span { display: block; white-space: nowrap; }.hero__copy > p:not(.kicker) { width: 100%; max-width: 390px; margin: 0 0 30px; color: rgba(25,25,25,.7); font-size: 16px; line-height: 1.55; }
.button { display: inline-flex; align-items: center; gap: 12px; min-height: 44px; padding: 0 20px; border-radius: 999px; font-size: 14px; font-weight: 600; transition: transform .25s, background .25s; }
.button:hover { transform: translateY(-2px); }.button--light { color: #fff; background: #191919; }.button span, .intro-band a span, .text-link span { font-size: 18px; line-height: 0; }
.hero__bridge { position: relative; z-index: 2; width: min(1024px, calc(100% - 48px)); margin: auto auto clamp(22px, 3.2vh, 38px); padding: 46px 48px 18px; overflow: hidden; background: rgba(255,255,255,.62); border: 1px solid rgba(25,25,25,.13); border-radius: 22px; box-shadow: 0 8px 30px rgba(25,25,25,.06); backdrop-filter: blur(5px) saturate(135%); -webkit-backdrop-filter: blur(5px) saturate(135%); }.hero__bridge-top { display: grid; grid-template-columns: 1fr .85fr; gap: 60px; align-items: end; }.hero__micro { margin: 0; color: rgba(25,25,25,.48); font-size: 11px; font-weight: 600; letter-spacing: .16em; }.hero__bridge h2 { margin: 13px 0 0; font-family: Georgia, 'Times New Roman', serif; font-size: clamp(26px, 3vw, 42px); font-weight: 400; letter-spacing: 0; line-height: 1.06; }.hero__bridge-top > p { margin: 0 0 3px; color: rgba(25,25,25,.66); font-size: 14px; line-height: 1.55; }.hero__bridge-links { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; margin-top: 34px; }.hero__bridge-links a { display: flex; justify-content: space-between; align-items: center; min-height: 62px; padding: 0 17px; background: rgba(244,243,243,.74); border: 1px solid rgba(25,25,25,.08); border-radius: 12px; font-size: 13px; font-weight: 600; transition: background .2s, transform .2s; }.hero__bridge-links a:hover { background: rgba(234,234,234,.9); }.hero__bridge-links span { margin-right: 9px; color: rgba(25,25,25,.42); font-weight: 400; }.hero__bridge-links b { color: rgba(25,25,25,.48); font-size: 16px; font-weight: 400; transition: transform .2s, color .2s; }.hero__bridge-links a:hover b { color: #191919; transform: translateX(3px); }
.intro-band { display: grid; grid-template-columns: 1.15fr .85fr; gap: 32px; align-items: end; max-width: 1280px; margin: 0 auto; padding: 52px 0 58px; }.intro-band .kicker { grid-column: 1 / -1; color: #6e6e73; }.intro-band h2, .story-panel h2 { margin: 0; font-size: clamp(35px, 4vw, 58px); font-weight: 600; letter-spacing: 0; line-height: 1.1; }.intro-band a { justify-self: end; padding-bottom: 8px; color: #2676c7; font-size: 16px; }
.featured-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; max-width: 1440px; margin: auto; padding: 0 12px; }.feature-card { min-height: 610px; overflow: hidden; background: #f5f5f7; }.feature-card :deep(.product-media) { height: 100%; }.feature-card :deep(img) { object-position: center; }.feature-card--0 { grid-row: span 2; min-height: 1232px; }.feature-card--0 :deep(img) { object-position: 54% center; }.feature-card__copy { position: absolute; z-index: 2; inset: 42px 42px auto; color: #1d1d1f; text-align: center; }.feature-card__copy.dark { color: #fff; }.feature-card__copy p { margin: 0 0 10px; font-size: 13px; }.feature-card__copy p span { margin-left: 8px; color: #bf4800; font-weight: 600; }.feature-card__copy h3 { margin: 0; font-size: clamp(28px, 3.2vw, 44px); font-weight: 600; letter-spacing: 0; }.feature-card__copy small { display: block; margin: 8px 0 18px; font-size: 15px; }.feature-card__copy b { font-size: 14px; font-weight: 500; }.feature-card__copy em { margin-left: 12px; color: #2676c7; font-style: normal; }
.story-panel { display: grid; grid-template-columns: 1fr 1fr; min-height: 650px; margin-top: 130px; background: #e9ece8; }.story-panel__copy { display: flex; flex-direction: column; align-items: flex-start; justify-content: center; padding: clamp(45px, 8vw, 150px); }.story-panel .kicker { color: #6e6e73; }.story-panel h2 { margin: 18px 0 24px; }.story-panel__copy > p:not(.kicker) { max-width: 380px; margin: 0 0 25px; color: #4a4a4d; font-size: 16px; line-height: 1.65; }.text-link { color: #2676c7; font-size: 15px; }.story-panel :deep(.product-media) { min-height: 100%; }.story-panel :deep(img) { object-position: center; }
.service-strip { display: grid; grid-template-columns: repeat(3, 1fr); max-width: 1280px; margin: auto; padding: 90px 0 110px; gap: 36px; }.service-strip div { display: grid; gap: 9px; }.service-strip strong { font-size: 17px; font-weight: 600; }.service-strip span { color: #6e6e73; font-size: 14px; }.consumer-footer { display: flex; justify-content: space-between; gap: 20px; padding: 22px max(24px, calc((100vw - 1280px) / 2)); color: #6e6e73; background: #f5f5f7; font-size: 12px; }.consumer-footer a { color: #4a4a4d; }
@media (max-width: 1320px) { .hero__copy { left: 52%; width: min(500px, 40vw); }.intro-band, .service-strip { width: calc(100% - 64px); }.intro-band { padding-top: 100px; }.service-strip { padding-top: 70px; } }
@media (max-width: 720px) { .home-view :deep(.consumer-nav__links) { background: rgba(255,255,255,.96); }.home-view :deep(.consumer-nav__links a) { color: #191919; }.home-view :deep(.nav-toggle) { color: #191919; }.hero { min-height: 720px; }.hero__copy { position: relative; top: auto; left: auto; width: calc(100% - 40px); margin: 0 auto; padding: 124px 0 0; }.hero h1 { font-size: 49px; }.hero h1 span { white-space: normal; }.hero__copy > p:not(.kicker) { font-size: 16px; }.hero__bridge { width: 100%; margin-bottom: 16px; padding: 28px 20px 14px; border-left: 0; border-right: 0; border-radius: 18px; }.hero__bridge-top { display: block; }.hero__bridge-top > p { max-width: 100%; margin: 18px 0 0; overflow-wrap: anywhere; word-break: break-word; }.hero__bridge-links { grid-template-columns: 1fr; gap: 8px; margin-top: 23px; }.hero__bridge-links a { min-height: 47px; border-radius: 10px; }.intro-band { display: block; width: calc(100% - 40px); padding: 48px 0 32px; }.intro-band h2 { margin: 15px 0 20px; font-size: 36px; }.featured-grid { grid-template-columns: 1fr; padding: 0 8px; gap: 8px; }.feature-card, .feature-card--0 { min-height: 520px; grid-row: auto; }.feature-card--0 { min-height: 620px; }.feature-card__copy { inset: 32px 18px auto; }.feature-card__copy h3 { font-size: 30px; }.story-panel { grid-template-columns: 1fr; margin-top: 80px; }.story-panel__copy { padding: 74px 20px 60px; }.story-panel h2 { font-size: 38px; }.story-panel :deep(.product-media) { min-height: 420px; }.service-strip { width: calc(100% - 40px); grid-template-columns: 1fr; padding: 56px 0; gap: 28px; }.consumer-footer { flex-wrap: wrap; padding: 20px; } }
</style>
