<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ConsumerNav from '../../components/ConsumerNav.vue'
import ProductMedia from '../../components/ProductMedia.vue'
import { addCartItem } from '../../api/cart'
import { getProductDetail, type PublicProductDetail, type PublicSku } from '../../api/product'
import { currency, findDemoProduct, makeDemoDetail } from '../../data/consumerCatalog'

const route = useRoute()
const router = useRouter()
const productId = computed(() => Number(route.params.spuId))
const product = ref<PublicProductDetail | null>(null)
const selected = ref<PublicSku | null>(null)
const quantity = ref(1)
const loading = ref(true)
const usingDemo = ref(false)
const submitting = ref(false)
const message = ref('')
const visual = computed(() => findDemoProduct(product.value?.id ?? productId.value))
const currentSku = computed(() => selected.value ?? product.value?.skus[0] ?? makeDemoDetail(productId.value).skus[0])
const canAdd = computed(() => currentSku.value.availableStock >= quantity.value && currentSku.value.availableStock > 0)

async function load() {
  loading.value = true
  usingDemo.value = false
  try {
    const remote = await getProductDetail(productId.value)
    if (!remote.skus.length) throw new Error('empty product')
    product.value = remote
  } catch {
    product.value = makeDemoDetail(productId.value)
    usingDemo.value = true
  } finally {
    selected.value = product.value?.skus[0] ?? null
    loading.value = false
  }
}

async function addToBag() {
  if (!canAdd.value || submitting.value) return
  message.value = ''
  submitting.value = true
  try {
    await addCartItem(currentSku.value.id, quantity.value)
    message.value = '已加入购物袋。'
  } catch {
    const items = JSON.parse(localStorage.getItem('morrow_demo_bag') ?? '[]')
    const existing = items.find((item: { skuId: number }) => item.skuId === currentSku.value.id)
    if (existing) existing.quantity += quantity.value
    else items.push({ id: Date.now(), skuId: currentSku.value.id, quantity: quantity.value, name: product.value?.name, skuName: currentSku.value.skuName, price: currentSku.value.salePrice })
    localStorage.setItem('morrow_demo_bag', JSON.stringify(items))
    message.value = '已加入演示购物袋。'
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="detail-view">
    <ConsumerNav />
    <main v-if="loading" class="detail-loading"><i /><i /></main>
    <main v-else class="detail-layout">
      <section class="detail-media"><ProductMedia :src="visual.detailImage" :alt="visual.imageAlt" :tone="visual.tone" eager /></section>
      <section class="detail-content">
        <RouterLink to="/stores/1/products" class="back-link">← 返回选购</RouterLink>
        <p v-if="usingDemo" class="demo-mark">演示商品</p>
        <p class="product-category">{{ visual.category }}</p>
        <h1>{{ product?.name }}</h1>
        <p class="product-tagline">{{ visual.tagline }}</p>
        <p class="product-description">{{ product?.description }}</p>
        <p class="price">{{ currency(currentSku.salePrice) }}</p>
        <div class="option-group"><p>选择款式</p><div><button v-for="sku in product?.skus" :key="sku.id" type="button" :class="{ active: selected?.id === sku.id }" @click="selected = sku">{{ sku.skuName }}</button></div></div>
        <div class="buy-row"><div class="quantity" aria-label="数量"><button type="button" aria-label="减少数量" @click="quantity = Math.max(1, quantity - 1)">−</button><span>{{ quantity }}</span><button type="button" aria-label="增加数量" :disabled="quantity >= currentSku.availableStock" @click="quantity += 1">+</button></div><button type="button" class="add-button" :disabled="!canAdd || submitting" @click="addToBag">{{ submitting ? '加入中…' : currentSku.availableStock ? '加入购物袋' : '暂时售罄' }}</button></div>
        <p v-if="message" class="bag-message">{{ message }} <button type="button" @click="router.push('/cart')">查看购物袋 →</button></p>
        <div class="detail-notes"><span>库存 {{ currentSku.availableStock }} 件</span><span>下单前将再次确认价格与库存</span></div>
      </section>
    </main>
  </div>
</template>

<style scoped>
.detail-view { min-height: 100vh; background: #fff; }.detail-layout { display: grid; grid-template-columns: minmax(0, 1.12fr) minmax(430px, .88fr); min-height: calc(100vh - 45px); }.detail-media { min-height: 760px; padding: 12px; }.detail-media :deep(.product-media) { height: 100%; }.detail-media :deep(img) { object-position: center; }.detail-content { display: flex; flex-direction: column; align-items: flex-start; justify-content: center; max-width: 510px; padding: 110px clamp(40px, 7vw, 115px) 80px; }.back-link { margin-bottom: 52px; color: #6e6e73; font-size: 14px; }.demo-mark { margin: 0 0 22px; color: #bf4800; font-size: 12px; }.product-category { margin: 0 0 9px; color: #6e6e73; font-size: 14px; }.detail-content h1 { margin: 0; font-size: clamp(39px, 4.3vw, 60px); font-weight: 600; letter-spacing: 0; line-height: 1.08; }.product-tagline { margin: 13px 0 0; font-size: 19px; line-height: 1.4; }.product-description { margin: 17px 0 28px; color: #6e6e73; font-size: 15px; line-height: 1.6; }.price { margin: 0 0 37px; font-size: 21px; font-weight: 600; }.option-group { width: 100%; }.option-group p { margin: 0 0 12px; color: #6e6e73; font-size: 14px; }.option-group button { min-width: 94px; min-height: 42px; margin-right: 8px; padding: 0 14px; color: #1d1d1f; border: 1px solid #d2d2d7; border-radius: 7px; background: #fff; }.option-group button.active { color: #fff; border-color: #1d1d1f; background: #1d1d1f; }.buy-row { display: grid; grid-template-columns: 130px 1fr; width: 100%; gap: 10px; margin-top: 33px; }.quantity { display: grid; grid-template-columns: 1fr 1fr 1fr; align-items: center; height: 47px; border: 1px solid #d2d2d7; border-radius: 999px; text-align: center; }.quantity button { height: 100%; border: 0; background: transparent; font-size: 21px; }.quantity span { font-size: 14px; }.add-button { min-height: 47px; color: #fff; border: 0; border-radius: 999px; background: #1d1d1f; font-size: 15px; font-weight: 600; }.add-button:hover:not(:disabled) { background: #353538; }.add-button:disabled { opacity: .46; cursor: not-allowed; }.bag-message { margin: 16px 0 0; color: #2676c7; font-size: 14px; }.bag-message button { margin-left: 6px; padding: 0; color: inherit; border: 0; background: transparent; text-decoration: underline; }.detail-notes { display: grid; gap: 8px; width: 100%; margin-top: 33px; padding-top: 18px; color: #6e6e73; border-top: 1px solid #d2d2d7; font-size: 12px; }.detail-loading { display: grid; grid-template-columns: 1.12fr .88fr; gap: 35px; min-height: calc(100vh - 45px); padding: 16px; }.detail-loading i { display: block; background: #f5f5f7; animation: pulse 1.4s ease-in-out infinite; }@keyframes pulse { 50% { opacity: .5; } }
@media (max-width: 820px) { .detail-layout { grid-template-columns: 1fr; }.detail-media { min-height: 480px; padding: 8px; }.detail-content { max-width: none; padding: 54px 20px 65px; }.back-link { margin-bottom: 39px; }.detail-content h1 { font-size: 44px; }.buy-row { grid-template-columns: 116px 1fr; }.detail-loading { grid-template-columns: 1fr; }.detail-loading i { min-height: 320px; } }
</style>
