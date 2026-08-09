<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ConsumerNav from '../../components/ConsumerNav.vue'
import { cancelOrder, getOrderDetail, mockPayOrder, orderErrorMessage, type CreateOrderResult, type OrderDetail } from '../../api/order'
import { currency } from '../../data/consumerCatalog'

const route = useRoute()
const router = useRouter()
const orderId = computed(() => Number(route.params.id))
const order = ref<OrderDetail | null>(null)
const createdOrder = ref<CreateOrderResult | null>(null)
const loading = ref(true)
const paying = ref(false)
const cancelling = ref(false)
const error = ref('')
const payError = ref('')

function statusLabel(status: string) {
  return ({ PENDING_PAYMENT: '待支付', PAID: '已支付', CLOSED: '已关闭' } as Record<string, string>)[status] ?? status
}

function formatDate(value: string) {
  return value ? new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : '时间待同步'
}

const displayStatus = computed(() => order.value?.status ?? createdOrder.value?.status ?? '')
const displayAmount = computed(() => order.value?.totalAmount ?? createdOrder.value?.totalAmount ?? 0)
const displayOrderNo = computed(() => order.value?.orderNo ?? createdOrder.value?.orderNo ?? '')
const isPendingPayment = computed(() => displayStatus.value === 'PENDING_PAYMENT')

function loadCreatedOrder() {
  try {
    const raw = sessionStorage.getItem(`morrow_created_order_${orderId.value}`)
    return raw ? JSON.parse(raw) as CreateOrderResult : null
  } catch { return null }
}

async function load() {
  loading.value = true
  error.value = ''
  createdOrder.value = loadCreatedOrder()
  try { order.value = await getOrderDetail(orderId.value) } catch (caught) {
    if (!createdOrder.value) error.value = orderErrorMessage(caught)
  } finally { loading.value = false }
}

async function pay() {
  if (!isPendingPayment.value || paying.value) return
  paying.value = true
  payError.value = ''
  try {
    await mockPayOrder(orderId.value)
    if (order.value) order.value = { ...order.value, status: 'PAID' }
    if (createdOrder.value) createdOrder.value = { ...createdOrder.value, status: 'PAID' }
  } catch (caught) { payError.value = orderErrorMessage(caught) } finally { paying.value = false }
}

async function cancel() {
  if (!isPendingPayment.value || cancelling.value) return
  cancelling.value = true
  payError.value = ''
  try {
    await cancelOrder(orderId.value)
    if (order.value) order.value = { ...order.value, status: 'CLOSED' }
    if (createdOrder.value) createdOrder.value = { ...createdOrder.value, status: 'CLOSED' }
  } catch (caught) { payError.value = orderErrorMessage(caught) } finally { cancelling.value = false }
}

onMounted(load)
</script>

<template>
  <div class="order-detail-view">
    <ConsumerNav />
    <main class="detail-shell">
      <div class="detail-back"><button type="button" @click="router.push('/orders')">← 返回订单</button></div>
      <section v-if="loading" class="detail-skeleton"><i /><i /></section>
      <section v-else-if="error" class="detail-feedback"><p>{{ error }}</p><button type="button" @click="load">重新加载</button></section>
      <template v-else>
        <header class="order-hero"><div><p class="kicker">{{ route.query.created ? 'ORDER CREATED' : 'ORDER DETAIL' }}</p><h1>{{ statusLabel(displayStatus) }}</h1><p class="order-no">{{ displayOrderNo }}</p></div><span class="status" :class="`status--${displayStatus.toLowerCase()}`">{{ statusLabel(displayStatus) }}</span></header>
        <section class="order-total"><span>订单合计</span><strong>{{ currency(displayAmount) }}</strong><p v-if="isPendingPayment">订单已为你保留库存，请在到期前完成支付。</p><p v-else-if="displayStatus === 'PAID'">支付已完成，感谢你把它带进生活。</p><p v-else>这笔订单当前无法支付。</p></section>
        <section v-if="order?.items.length" class="order-items"><p class="section-label">商品明细</p><article v-for="item in order.items" :key="item.id"><div><h2>{{ item.skuNameSnapshot }}</h2><p>数量 {{ item.quantity }}</p></div><strong>{{ currency(item.salePrice * item.quantity) }}</strong></article></section>
        <section class="order-timeline"><p class="section-label">订单信息</p><div><span>创建时间</span><strong>{{ formatDate(order?.createdAt ?? '') }}</strong></div><div v-if="isPendingPayment"><span>支付截止</span><strong>{{ formatDate(order?.expireAt ?? createdOrder?.expireAt ?? '') }}</strong></div></section>
        <section v-if="isPendingPayment" class="payment-panel"><div><p class="section-label">MOCK PAYMENT</p><h2>确认这次选择</h2><p>这是课程项目的模拟支付，不会产生真实扣款。</p></div><div class="payment-actions"><button type="button" :disabled="paying || cancelling" @click="pay">{{ paying ? '正在确认…' : `模拟支付 ${currency(displayAmount)}` }}</button><button type="button" class="cancel-button" :disabled="paying || cancelling" @click="cancel">{{ cancelling ? '取消中…' : '取消订单' }}</button></div><p v-if="payError" class="payment-error">{{ payError }}</p></section>
      </template>
    </main>
  </div>
</template>

<style scoped>
.order-detail-view { min-height: 100vh; background: #fff; }.detail-shell { width: min(840px, calc(100% - 64px)); margin: auto; padding: 48px 0 110px; }.detail-back button { padding: 0; color: #6e6e73; border: 0; background: transparent; font-size: 14px; }.order-hero { display: flex; align-items: flex-start; justify-content: space-between; gap: 26px; margin: 63px 0 47px; }.kicker, .section-label { margin: 0; color: #6e6e73; font-size: 12px; font-weight: 600; letter-spacing: .12em; }.order-hero h1 { margin: 14px 0 10px; font-size: 50px; font-weight: 600; line-height: 1; }.order-no { margin: 0; color: #6e6e73; font-size: 14px; }.status { display: inline-flex; align-items: center; min-height: 28px; padding: 0 11px; border-radius: 999px; font-size: 12px; font-weight: 600; }.status--pending_payment { color: #854d00; background: #fff1d6; }.status--paid { color: #146c43; background: #dff5e7; }.status--closed { color: #6e6e73; background: #ececef; }.order-total { display: grid; gap: 8px; padding: 31px 33px; border-radius: 12px; background: #f5f5f7; }.order-total > span { color: #6e6e73; font-size: 14px; }.order-total strong { font-size: 33px; font-weight: 600; }.order-total p { margin: 4px 0 0; color: #6e6e73; font-size: 14px; line-height: 1.55; }.order-items, .order-timeline { margin-top: 52px; }.section-label { margin-bottom: 19px; }.order-items article { display: flex; align-items: center; justify-content: space-between; gap: 22px; padding: 22px 0; border-top: 1px solid #d2d2d7; }.order-items article:last-child { border-bottom: 1px solid #d2d2d7; }.order-items h2 { margin: 0; font-size: 16px; font-weight: 600; }.order-items p { margin: 7px 0 0; color: #6e6e73; font-size: 13px; }.order-items strong { font-size: 15px; font-weight: 500; white-space: nowrap; }.order-timeline { border-top: 1px solid #d2d2d7; }.order-timeline .section-label { padding-top: 24px; }.order-timeline > div { display: flex; justify-content: space-between; gap: 20px; margin-top: 16px; color: #6e6e73; font-size: 14px; }.order-timeline strong { color: #1d1d1f; font-weight: 500; text-align: right; }.payment-panel { display: grid; grid-template-columns: 1fr auto; align-items: center; gap: 24px; margin-top: 58px; padding: 29px 32px; border-radius: 12px; background: #1d1d1f; color: #fff; }.payment-panel .section-label { color: rgba(255,255,255,.55); }.payment-panel h2 { margin: 12px 0 8px; font-size: 22px; font-weight: 600; }.payment-panel p:not(.section-label) { margin: 0; color: rgba(255,255,255,.68); font-size: 14px; }.payment-panel button { min-width: 173px; min-height: 46px; padding: 0 18px; color: #1d1d1f; border: 0; border-radius: 999px; background: #fff; font-size: 14px; font-weight: 600; }.payment-panel .payment-error { grid-column: 1 / -1; color: #ffb57c; }.detail-skeleton { display: grid; gap: 26px; margin-top: 61px; }.detail-skeleton i { display: block; height: 185px; border-radius: 12px; background: #f5f5f7; animation: pulse 1.4s ease-in-out infinite; }.detail-skeleton i:last-child { height: 90px; }.detail-feedback { display: grid; place-items: center; min-height: 340px; gap: 18px; color: #6e6e73; text-align: center; }.detail-feedback p { margin: 0; }.detail-feedback button { min-height: 42px; padding: 0 19px; color: #1d1d1f; border: 1px solid #d2d2d7; border-radius: 999px; background: #fff; }@keyframes pulse { 50% { opacity: .48; } }
@media (max-width: 620px) { .detail-shell { width: calc(100% - 40px); padding: 38px 0 66px; }.order-hero { margin: 51px 0 36px; }.order-hero h1 { font-size: 42px; }.order-total { padding: 27px 24px; border-radius: 10px; }.order-total strong { font-size: 29px; }.payment-panel { grid-template-columns: 1fr; gap: 21px; padding: 28px 24px; border-radius: 10px; }.payment-panel button { width: 100%; }.order-timeline > div { align-items: flex-start; flex-direction: column; gap: 7px; }.order-timeline strong { text-align: left; } }
</style>
<style scoped>.payment-actions{display:grid;gap:8px}.payment-actions .cancel-button{color:#fff;border:1px solid #555;background:transparent}</style>
