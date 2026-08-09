<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import ConsumerNav from '../../components/ConsumerNav.vue'
import ProductMedia from '../../components/ProductMedia.vue'
import { getStoreProducts } from '../../api/product'
import { currency, demoProducts, enrichProducts, type ConsumerProduct } from '../../data/consumerCatalog'

const products = ref<ConsumerProduct[]>([])
const route = useRoute()
const storeId = computed(() => Number(route.params.storeId) || 1001)
const loading = ref(true)
const usingDemo = ref(false)
const keyword = ref('')
const category = ref('全部')
const categories = computed(() => ['全部', ...new Set(products.value.map((item) => item.category))])
const visibleProducts = computed(() => products.value.filter((item) => {
  const matchCategory = category.value === '全部' || item.category === category.value
  const query = keyword.value.trim()
  const matchSearch = !query || `${item.name}${item.description}${item.category}`.includes(query)
  return matchCategory && matchSearch
}))

async function load() {
  loading.value = true
  usingDemo.value = false
  try {
    const remote = await getStoreProducts(storeId.value)
    if (remote.length) products.value = enrichProducts(remote)
    else {
      products.value = demoProducts
      usingDemo.value = true
    }
  } catch {
    products.value = demoProducts
    usingDemo.value = true
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="catalog-view">
    <ConsumerNav />
    <main>
      <header class="catalog-hero">
        <div>
          <p class="kicker">MORROW STORE</p>
          <h1>选一些真正会<br />陪你生活的东西。</h1>
        </div>
        <p>为专注、出发、停留和每一个平常时刻，慢慢挑选。</p>
      </header>

      <section class="catalog-toolbar" aria-label="商品筛选">
        <div class="category-list">
          <button v-for="item in categories" :key="item" type="button" :class="{ active: category === item }" @click="category = item">{{ item }}</button>
        </div>
        <label class="catalog-search">
          <span aria-hidden="true">⌕</span>
          <input v-model="keyword" type="search" placeholder="搜索商品" aria-label="搜索商品" />
        </label>
      </section>

      <p v-if="usingDemo && !loading" class="demo-notice">当前显示演示商品；连接到有库存的店铺后将自动显示真实数据。</p>

      <section v-if="loading" class="catalog-grid catalog-grid--loading" aria-label="正在加载商品">
        <div v-for="item in 6" :key="item" />
      </section>
      <section v-else-if="visibleProducts.length" class="catalog-grid">
        <RouterLink v-for="item in visibleProducts" :key="item.id" :to="`/stores/${storeId}/products/${item.id}`" class="catalog-card">
          <ProductMedia :src="item.image" :alt="item.imageAlt" :tone="item.tone">
            <span v-if="item.isNew" class="new-badge">新品</span>
          </ProductMedia>
          <div class="catalog-card__info">
            <div><p>{{ item.category }}</p><h2>{{ item.name }}</h2></div>
            <strong>{{ currency(item.minSalePrice) }}</strong>
          </div>
          <p class="catalog-card__description">{{ item.description }}</p>
        </RouterLink>
      </section>
      <section v-else class="catalog-empty">
        <h2>没有找到相符的商品</h2>
        <button type="button" @click="keyword = ''; category = '全部'">清除筛选</button>
      </section>
    </main>
  </div>
</template>

<style scoped>
.catalog-view { min-height: 100vh; background: #fff; }.catalog-hero { display: flex; justify-content: space-between; align-items: end; max-width: 1280px; margin: auto; min-height: 390px; padding: 118px 0 62px; }.kicker { margin: 0 0 16px; color: #6e6e73; font-size: 12px; font-weight: 600; letter-spacing: .12em; }.catalog-hero h1 { margin: 0; font-size: clamp(42px, 5vw, 71px); font-weight: 600; letter-spacing: 0; line-height: 1.08; }.catalog-hero > p { max-width: 258px; margin: 0 0 6px; color: #6e6e73; font-size: 16px; line-height: 1.55; }.catalog-toolbar { position: sticky; top: 45px; z-index: 10; display: flex; justify-content: space-between; align-items: center; max-width: 1280px; margin: auto; padding: 15px 0; border-top: 1px solid #d2d2d7; border-bottom: 1px solid #d2d2d7; background: rgba(255,255,255,.9); backdrop-filter: blur(20px); }.category-list { display: flex; gap: 4px; }.category-list button { min-height: 34px; padding: 0 12px; color: #6e6e73; border: 0; border-radius: 999px; background: transparent; font-size: 14px; }.category-list button:hover, .category-list button.active { color: #fff; background: #1d1d1f; }.catalog-search { display: flex; align-items: center; gap: 8px; width: 190px; color: #6e6e73; }.catalog-search input { width: 100%; padding: 7px 0; color: #1d1d1f; border: 0; outline: 0; background: transparent; font-size: 14px; }.demo-notice { max-width: 1280px; margin: 16px auto 0; color: #6e6e73; font-size: 12px; }.catalog-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 58px 16px; max-width: 1280px; margin: 42px auto 100px; }.catalog-card :deep(.product-media) { aspect-ratio: 1 / 1.06; }.catalog-card__info { display: flex; justify-content: space-between; gap: 16px; margin-top: 16px; }.catalog-card__info p { margin: 0 0 6px; color: #6e6e73; font-size: 12px; }.catalog-card__info h2 { margin: 0; font-size: 18px; font-weight: 600; }.catalog-card__info strong { padding-top: 17px; font-size: 15px; font-weight: 500; white-space: nowrap; }.catalog-card__description { margin: 8px 0 0; color: #6e6e73; font-size: 14px; }.new-badge { position: absolute; z-index: 3; top: 14px; left: 14px; padding: 6px 9px; color: #fff; border-radius: 999px; background: rgba(29,29,31,.76); font-size: 11px; }.catalog-grid--loading div { aspect-ratio: 1 / 1.24; background: #f5f5f7; animation: pulse 1.4s ease-in-out infinite; }.catalog-empty { min-height: 360px; display: grid; place-content: center; gap: 16px; text-align: center; }.catalog-empty h2 { margin: 0; font-size: 23px; }.catalog-empty button { color: #2676c7; border: 0; background: transparent; font-size: 15px; }@keyframes pulse { 50% { opacity: .52; } }
@media (max-width: 1320px) { .catalog-hero, .catalog-toolbar, .demo-notice, .catalog-grid { width: calc(100% - 64px); } }.catalog-hero { padding-top: 100px; }.catalog-grid { gap: 44px 14px; }@media (max-width: 720px) { .catalog-hero, .catalog-toolbar, .demo-notice, .catalog-grid { width: calc(100% - 40px); }.catalog-hero { display: block; min-height: 315px; padding: 94px 0 42px; }.catalog-hero h1 { font-size: 43px; }.catalog-hero > p { margin-top: 19px; font-size: 15px; }.catalog-toolbar { top: 45px; display: block; padding: 11px 0; }.category-list { overflow-x: auto; padding-bottom: 8px; }.category-list button { flex: 0 0 auto; }.catalog-search { width: 100%; padding-top: 4px; }.catalog-grid { grid-template-columns: repeat(2, 1fr); gap: 35px 12px; margin-top: 28px; }.catalog-card__info { display: block; }.catalog-card__info h2 { font-size: 16px; }.catalog-card__info strong { display: block; padding: 10px 0 0; }.catalog-card__description { font-size: 13px; line-height: 1.45; } }
</style>
