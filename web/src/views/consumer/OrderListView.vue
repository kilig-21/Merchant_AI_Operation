<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import ConsumerNav from '../../components/ConsumerNav.vue'
import { getOrders, orderErrorMessage, type OrderDetail } from '../../api/order'
import { currency } from '../../data/consumerCatalog'

const orders = ref<OrderDetail[]>([])
const loading = ref(true)
const error = ref('')

function statusLabel(status: string) {
  return ({ PENDING_PAYMENT: '待支付', PAID: '已支付', CLOSED: '已关闭' } as Record<string, string>)[status] ?? status
}

function formatDate(value: string) {
  return value ? new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : '时间待同步'
}

const hasOrders = computed(() => orders.value.length > 0)

async function load() {
  loading.value = true
  error.value = ''
  try { orders.value = await getOrders() } catch (caught) { error.value = orderErrorMessage(caught) } finally { loading.value = false }
}

onMounted(load)
</script>

<template>
  <div class="orders-view">
    <ConsumerNav />
    <main class="orders-shell">
      <header class="orders-header"><p class="kicker">YOUR ORDERS</p><h1>订单</h1><p>每一次选择，都有清楚的去处。</p></header>
      <section v-if="loading" class="order-skeleton"><i /><i /><i /></section>
      <section v-else-if="error" class="order-feedback"><p>{{ error }}</p><button type="button" @click="load">重新加载</button></section>
      <section v-else-if="hasOrders" class="order-list" aria-label="订单列表">
        <RouterLink v-for="order in orders" :key="order.id" :to="`/orders/${order.id}`" class="order-row">
          <div class="order-row__lead"><span class="status" :class="`status--${order.status.toLowerCase()}`">{{ statusLabel(order.status) }}</span><h2>{{ currency(order.totalAmount) }}</h2></div>
          <div class="order-row__meta"><p>{{ order.orderNo }}</p><span>{{ formatDate(order.createdAt) }}</span></div>
          <span class="order-row__arrow" aria-hidden="true">→</span>
        </RouterLink>
      </section>
      <section v-else class="orders-empty"><p class="kicker">NOTHING HERE YET</p><h2>还没有留下订单。</h2><p>从一件适合今天的物品开始。</p><RouterLink to="/stores/1/products">去选购 →</RouterLink></section>
    </main>
  </div>
</template>

<style scoped>
.orders-view { min-height: 100vh; background: #fff; }.orders-shell { width: min(920px, calc(100% - 64px)); margin: auto; padding: 108px 0 100px; }.kicker { margin: 0 0 13px; color: #6e6e73; font-size: 12px; font-weight: 600; letter-spacing: .12em; }.orders-header { margin-bottom: 58px; }.orders-header h1 { margin: 0; font-size: 52px; font-weight: 600; line-height: 1; }.orders-header > p:last-child { margin: 17px 0 0; color: #6e6e73; font-size: 16px; }.order-list { border-top: 1px solid #d2d2d7; }.order-row { display: grid; grid-template-columns: 1fr auto 28px; align-items: center; gap: 26px; min-height: 132px; border-bottom: 1px solid #d2d2d7; transition: background-color .2s, padding .2s; }.order-row:hover { padding: 0 16px; background: #f5f5f7; }.order-row__lead { display: flex; align-items: center; gap: 17px; }.status { display: inline-flex; align-items: center; min-width: 58px; min-height: 26px; justify-content: center; padding: 0 10px; border-radius: 999px; font-size: 12px; font-weight: 600; }.status--pending_payment { color: #854d00; background: #fff1d6; }.status--paid { color: #146c43; background: #dff5e7; }.status--closed { color: #6e6e73; background: #ececef; }.order-row h2 { margin: 0; font-size: 23px; font-weight: 600; }.order-row__meta { text-align: right; }.order-row__meta p { margin: 0 0 8px; font-size: 13px; }.order-row__meta span { color: #6e6e73; font-size: 12px; }.order-row__arrow { font-size: 21px; }.order-skeleton { display: grid; gap: 1px; }.order-skeleton i { display: block; height: 132px; background: #f5f5f7; animation: pulse 1.4s ease-in-out infinite; }.order-feedback { display: grid; place-items: center; min-height: 280px; gap: 18px; color: #6e6e73; text-align: center; }.order-feedback p { margin: 0; }.order-feedback button { min-height: 42px; padding: 0 19px; color: #1d1d1f; border: 1px solid #d2d2d7; border-radius: 999px; background: #fff; }.orders-empty { min-height: 390px; display: grid; place-content: center; gap: 15px; text-align: center; }.orders-empty .kicker { margin: 0; }.orders-empty h2 { margin: 0; font-size: 29px; }.orders-empty > p:not(.kicker) { margin: 0; color: #6e6e73; }.orders-empty a { margin-top: 8px; color: #2676c7; font-size: 15px; }@keyframes pulse { 50% { opacity: .48; } }
@media (max-width: 620px) { .orders-shell { width: calc(100% - 40px); padding: 87px 0 65px; }.orders-header { margin-bottom: 38px; }.orders-header h1 { font-size: 44px; }.order-row { grid-template-columns: 1fr 24px; gap: 12px; padding: 21px 0; }.order-row:hover { padding: 21px 10px; }.order-row__lead { align-items: flex-start; flex-direction: column; gap: 10px; }.order-row__meta { grid-column: 1; text-align: left; }.order-row__arrow { grid-column: 2; grid-row: 1 / 3; }.order-row__meta p { overflow: hidden; max-width: 210px; margin-bottom: 5px; text-overflow: ellipsis; white-space: nowrap; } }
</style>
