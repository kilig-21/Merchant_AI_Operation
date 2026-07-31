<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import ConsumerNav from '../../components/ConsumerNav.vue'
import ProductMedia from '../../components/ProductMedia.vue'
import { deleteCartItem, getCartItems, updateCartQuantity } from '../../api/cart'
import { currency, demoProducts, findDemoProductBySku } from '../../data/consumerCatalog'

interface DisplayItem { id: number; skuId: number; quantity: number; name: string; skuName: string; price: number }
const items = ref<DisplayItem[]>([])
const loading = ref(true)
const usingDemo = ref(false)
const checkoutNotice = ref('')
const total = computed(() => items.value.reduce((sum, item) => sum + item.price * item.quantity, 0))
const count = computed(() => items.value.reduce((sum, item) => sum + item.quantity, 0))
const demoItems = (): DisplayItem[] => demoProducts.slice(0, 2).map((item, index) => ({ id: index + 1, skuId: item.id * 100 + 1, quantity: index + 1, name: item.name, skuName: '浅色款', price: item.minSalePrice ?? 0 }))

function savedDemoItems() {
  try { return JSON.parse(localStorage.getItem('morrow_demo_bag') ?? 'null') as DisplayItem[] | null } catch { return null }
}
function saveDemoItems() { localStorage.setItem('morrow_demo_bag', JSON.stringify(items.value)) }

async function load() {
  loading.value = true
  usingDemo.value = false
  try {
    const remote = await getCartItems()
    if (remote.length) items.value = remote.map((item) => {
      const product = findDemoProductBySku(item.skuId)
      return { ...item, name: product.name, skuName: '已选款式', price: product.minSalePrice ?? 0 }
    })
    else {
      items.value = savedDemoItems() ?? demoItems()
      usingDemo.value = true
    }
  } catch {
    items.value = savedDemoItems() ?? demoItems()
    usingDemo.value = true
  } finally { loading.value = false }
}

async function changeQuantity(item: DisplayItem, delta: number) {
  const next = Math.max(1, item.quantity + delta)
  item.quantity = next
  if (usingDemo.value) { saveDemoItems(); return }
  try { await updateCartQuantity(item.id, next) } catch { usingDemo.value = true; saveDemoItems() }
}
async function removeItem(item: DisplayItem) {
  const previous = [...items.value]
  items.value = items.value.filter((entry) => entry.id !== item.id)
  if (usingDemo.value) { saveDemoItems(); return }
  try { await deleteCartItem(item.id) } catch { items.value = previous; usingDemo.value = true; saveDemoItems() }
}
function checkout() { checkoutNotice.value = '订单接口尚未开放；购物袋与库存接口已经可以直接对接。' }
onMounted(load)
</script>

<template>
  <div class="bag-view">
    <ConsumerNav />
    <main class="bag-shell">
      <header class="bag-header"><p class="kicker">YOUR BAG</p><h1>购物袋 <span v-if="!loading">{{ count }} 件</span></h1></header>
      <p v-if="usingDemo && !loading" class="demo-notice">当前为演示购物袋；登录并连接后端后会显示你的真实购物袋。</p>
      <section v-if="loading" class="bag-loading"><i /><i /><i /></section>
      <section v-else-if="items.length" class="bag-layout">
        <div class="bag-items">
          <article v-for="item in items" :key="item.id" class="bag-item">
            <ProductMedia :src="findDemoProductBySku(item.skuId).image" :alt="item.name" :tone="findDemoProductBySku(item.skuId).tone" />
            <div class="bag-item__info"><p>{{ item.skuName }}</p><h2>{{ item.name }}</h2><button type="button" @click="removeItem(item)">移除</button></div>
            <strong>{{ currency(item.price) }}</strong>
            <div class="bag-stepper"><button type="button" aria-label="减少数量" @click="changeQuantity(item, -1)">−</button><span>{{ item.quantity }}</span><button type="button" aria-label="增加数量" @click="changeQuantity(item, 1)">+</button></div>
          </article>
        </div>
        <aside class="bag-summary"><h2>订单摘要</h2><p><span>商品小计</span><strong>{{ currency(total) }}</strong></p><p><span>配送</span><strong>免运费</strong></p><div><span>合计</span><strong>{{ currency(total) }}</strong></div><button type="button" @click="checkout">继续结算</button><small>税费将在提交订单前确认。</small><p v-if="checkoutNotice" class="checkout-notice">{{ checkoutNotice }}</p></aside>
      </section>
      <section v-else class="bag-empty"><p class="kicker">EMPTY FOR NOW</p><h2>这里还没有你的选择。</h2><p>从一件能让今天变好的物件开始。</p><RouterLink to="/stores/1/products">去选购 →</RouterLink></section>
    </main>
  </div>
</template>

<style scoped>
.bag-view { min-height: 100vh; background: #fff; }.bag-shell { max-width: 1120px; margin: auto; padding: 104px 0 110px; }.kicker { margin: 0 0 12px; color: #6e6e73; font-size: 12px; font-weight: 600; letter-spacing: .12em; }.bag-header h1 { margin: 0; font-size: 48px; font-weight: 600; letter-spacing: 0; }.bag-header h1 span { color: #6e6e73; font-size: 18px; font-weight: 400; }.demo-notice { margin: 17px 0 0; color: #6e6e73; font-size: 12px; }.bag-layout { display: grid; grid-template-columns: 1fr 330px; gap: 76px; margin-top: 54px; }.bag-items { min-width: 0; }.bag-item { display: grid; grid-template-columns: 128px 1fr 88px 118px; align-items: center; gap: 20px; padding: 0 0 28px; margin-bottom: 28px; border-bottom: 1px solid #d2d2d7; }.bag-item :deep(.product-media) { width: 128px; aspect-ratio: 1; }.bag-item__info p { margin: 0 0 7px; color: #6e6e73; font-size: 13px; }.bag-item__info h2 { margin: 0; font-size: 17px; font-weight: 600; }.bag-item__info button { margin-top: 16px; padding: 0; color: #6e6e73; border: 0; background: transparent; font-size: 13px; text-decoration: underline; }.bag-item > strong { font-size: 15px; font-weight: 500; text-align: right; }.bag-stepper { display: grid; grid-template-columns: repeat(3, 1fr); align-items: center; height: 38px; border: 1px solid #d2d2d7; border-radius: 999px; text-align: center; }.bag-stepper button { height: 100%; border: 0; background: transparent; font-size: 18px; }.bag-stepper span { font-size: 13px; }.bag-summary { height: max-content; padding: 25px; background: #f5f5f7; }.bag-summary h2 { margin: 0 0 26px; font-size: 18px; font-weight: 600; }.bag-summary p, .bag-summary > div { display: flex; justify-content: space-between; gap: 12px; margin: 13px 0; color: #6e6e73; font-size: 14px; }.bag-summary p strong { color: #1d1d1f; font-weight: 500; }.bag-summary > div { margin-top: 22px; padding-top: 18px; color: #1d1d1f; border-top: 1px solid #d2d2d7; font-size: 16px; }.bag-summary > div strong { font-size: 19px; }.bag-summary > button { width: 100%; min-height: 45px; margin-top: 22px; color: #fff; border: 0; border-radius: 999px; background: #1d1d1f; font-size: 15px; font-weight: 600; }.bag-summary small { display: block; margin-top: 15px; color: #6e6e73; font-size: 11px; text-align: center; }.checkout-notice { display: block !important; margin: 16px 0 0 !important; color: #bf4800 !important; line-height: 1.5; }.bag-loading { display: grid; gap: 28px; margin-top: 54px; }.bag-loading i { display: block; height: 128px; background: #f5f5f7; animation: pulse 1.4s ease-in-out infinite; }.bag-empty { min-height: 410px; display: grid; place-content: center; gap: 14px; text-align: center; }.bag-empty .kicker { margin: 0; }.bag-empty h2 { margin: 0; font-size: 28px; }.bag-empty > p:not(.kicker) { margin: 0; color: #6e6e73; }.bag-empty a { margin-top: 8px; color: #2676c7; font-size: 15px; }@keyframes pulse { 50% { opacity: .48; } }
@media (max-width: 1180px) { .bag-shell { width: calc(100% - 64px); } }.bag-layout { gap: 45px; }@media (max-width: 760px) { .bag-shell { width: calc(100% - 40px); padding: 90px 0 65px; }.bag-header h1 { font-size: 42px; }.bag-layout { grid-template-columns: 1fr; gap: 34px; margin-top: 37px; }.bag-summary { order: -1; }.bag-item { grid-template-columns: 88px 1fr 70px; gap: 13px; }.bag-item :deep(.product-media) { width: 88px; }.bag-item > strong { text-align: right; }.bag-stepper { grid-column: 2 / 4; width: 118px; }.bag-item__info h2 { font-size: 16px; } }
</style>
