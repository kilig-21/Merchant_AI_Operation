<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { register } from '../../api/auth'
import { useAuthStore } from '../../stores/auth'
import ProductMedia from '../../components/ProductMedia.vue'
import { heroMedia } from '../../data/consumerCatalog'

const props = defineProps<{ mode: 'login' | 'register' }>()
const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)
const isLogin = computed(() => props.mode === 'login')
async function submit() {
  error.value = ''
  if (!username.value || !password.value) { error.value = '请填写账号和密码。'; return }
  loading.value = true
  try {
    if (isLogin.value) await auth.signIn({ username: username.value, password: password.value })
    else { await register({ username: username.value, password: password.value }); await auth.signIn({ username: username.value, password: password.value }) }
    router.push(typeof route.query.redirect === 'string' ? route.query.redirect : '/stores/1001/products')
  } catch { error.value = isLogin.value ? '登录失败，请检查账号和密码。' : '注册失败，请确认账号为 3-64 位、密码为 6-32 位。' } finally { loading.value = false }
}
</script>

<template>
  <main class="account-view">
    <ProductMedia class="account-scene" :src="heroMedia" alt="明亮的生活空间" tone="#d5e1da" eager><div><RouterLink to="/">Morrow</RouterLink><p>把喜欢的日常，留在这里。</p></div></ProductMedia>
    <section class="account-form"><RouterLink to="/" class="mobile-brand">Morrow</RouterLink><form @submit.prevent="submit"><p class="kicker">{{ isLogin ? 'WELCOME BACK' : 'JOIN MORROW' }}</p><h1>{{ isLogin ? '继续你的选择。' : '从今天开始，慢慢选。' }}</h1><p class="intro">{{ isLogin ? '登录后查看购物袋与订单状态。' : '注册一个账户，保存你想慢慢决定的东西。' }}</p><label>账号<input v-model.trim="username" autocomplete="username" placeholder="输入账号" /></label><label>密码<input v-model="password" type="password" autocomplete="current-password" placeholder="输入密码" /></label><p v-if="error" class="error">{{ error }}</p><button type="submit" :disabled="loading">{{ loading ? '处理中…' : isLogin ? '登录' : '创建账户' }}</button><p class="switch">{{ isLogin ? '第一次来到这里？' : '已经有账户？' }} <RouterLink :to="isLogin ? '/consumer/register' : '/consumer/login'">{{ isLogin ? '去注册' : '去登录' }}</RouterLink></p></form></section>
  </main>
</template>

<style scoped>
.account-view { display: grid; grid-template-columns: 1.06fr .94fr; min-height: 100vh; }.account-scene { min-height: 100vh; color: #fff; }.account-scene::before { content: ''; position: absolute; z-index: 1; inset: 0; background: rgba(21,31,28,.29); }.account-scene > div { position: absolute; z-index: 2; inset: 40px 48px auto; display: flex; justify-content: space-between; align-items: center; }.account-scene a, .mobile-brand { font-size: 18px; font-weight: 600; }.account-scene p { margin: 0; font-size: 14px; }.account-form { display: flex; align-items: center; justify-content: center; padding: 55px 10%; }.mobile-brand { display: none; }.account-form form { width: min(390px, 100%); }.kicker { margin: 0 0 15px; color: #6e6e73; font-size: 12px; font-weight: 600; letter-spacing: .12em; }.account-form h1 { margin: 0; font-size: 41px; font-weight: 600; letter-spacing: 0; line-height: 1.13; }.intro { margin: 15px 0 34px; color: #6e6e73; font-size: 15px; line-height: 1.6; }.account-form form { display: grid; gap: 17px; }.account-form label { display: grid; gap: 8px; color: #424245; font-size: 13px; }.account-form input { min-height: 48px; padding: 0 14px; color: #1d1d1f; border: 1px solid #d2d2d7; border-radius: 8px; outline: 0; font-size: 15px; }.account-form input:focus { border-color: #2676c7; box-shadow: 0 0 0 4px rgba(38,118,199,.12); }.account-form button { min-height: 47px; margin-top: 7px; color: #fff; border: 0; border-radius: 999px; background: #1d1d1f; font-size: 15px; font-weight: 600; }.account-form button:disabled { opacity: .5; }.error { margin: 0; color: #bf4800; font-size: 13px; }.switch { margin: 4px 0 0; color: #6e6e73; font-size: 14px; }.switch a { color: #2676c7; }@media (max-width: 760px) { .account-view { grid-template-columns: 1fr; }.account-scene { min-height: 220px; }.account-scene > div { inset: 22px 22px auto; }.account-scene p { display: none; }.account-form { min-height: calc(100vh - 220px); align-items: flex-start; padding: 31px 20px 50px; }.mobile-brand { display: block; margin-bottom: 70px; }.account-form form { width: 100%; }.account-form h1 { font-size: 37px; } }
</style>
