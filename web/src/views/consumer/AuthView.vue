<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { register } from '../../api/auth'
import { useAuthStore } from '../../stores/auth'
const props = defineProps<{ mode: 'login' | 'register' }>()
const router = useRouter(); const route = useRoute(); const auth = useAuthStore()
const username = ref(''); const password = ref(''); const error = ref(''); const loading = ref(false)
const isLogin = computed(() => props.mode === 'login')
async function submit() { error.value = ''; if (!username.value || !password.value) { error.value = '请填写账号与密码'; return }; loading.value = true; try { if (isLogin.value) { await auth.signIn({ username: username.value, password: password.value }) } else { await register({ username: username.value, password: password.value }); await auth.signIn({ username: username.value, password: password.value }) }; router.push(typeof route.query.redirect === 'string' ? route.query.redirect : '/stores/1/products') } catch { error.value = isLogin.value ? '登录失败，请检查账号和密码' : '注册失败，请确认账号为 3–64 位、密码为 6–32 位' } finally { loading.value = false } }
</script>
<template>
  <main class="consumer-auth">
    <section class="consumer-auth__scene"><div><p>日常所需，恰到好处。</p><span>精选小店 · 稳妥送达</span></div></section>
    <section class="consumer-auth__form-area"><RouterLink to="/stores/1/products" class="consumer-auth__brand">ShelfFlow</RouterLink><form @submit.prevent="submit"><p class="eyebrow">{{ isLogin ? '欢迎回来' : '创建账户' }}</p><h1>{{ isLogin ? '登录后继续选购' : '注册，开始选购' }}</h1><p class="intro">{{ isLogin ? '使用你的账户查看购物车和订单。' : '仅需一个账户，即可保存你的购物车。' }}</p><label class="field">账号<input v-model.trim="username" autocomplete="username" placeholder="输入账号" /></label><label class="field">密码<input v-model="password" type="password" autocomplete="current-password" placeholder="输入密码" /></label><p v-if="error" class="page-error">{{ error }}</p><button class="primary-button auth-submit" :disabled="loading">{{ loading ? '处理中…' : isLogin ? '登录' : '创建账户' }}</button><p class="switch">{{ isLogin ? '还没有账户？' : '已有账户？' }} <RouterLink :to="isLogin ? '/consumer/register' : '/consumer/login'">{{ isLogin ? '去注册' : '去登录' }}</RouterLink></p></form></section>
  </main>
</template>
<style scoped>
.consumer-auth{min-height:100dvh;display:grid;grid-template-columns:1.1fr .9fr;background:#fff}.consumer-auth__scene{position:relative;min-height:100%;background:url('https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=1600&q=85') center/cover}.consumer-auth__scene:after{content:'';position:absolute;inset:0;background:rgba(23,26,32,.3)}.consumer-auth__scene div{position:absolute;z-index:1;left:58px;bottom:54px;color:#fff}.consumer-auth__scene p{font-size:34px;font-weight:500;letter-spacing:-.6px;margin:0 0 9px}.consumer-auth__scene span{font-size:14px;color:rgba(255,255,255,.76)}.consumer-auth__form-area{padding:42px 12%;display:flex;flex-direction:column}.consumer-auth__brand{font-size:17px;font-weight:500}.consumer-auth form{width:min(380px,100%);margin:auto 0}.eyebrow{margin:0 0 12px;color:#5c5e62;font-size:14px}.consumer-auth h1{font-size:34px;font-weight:500;letter-spacing:-.5px;margin:0}.intro{margin:14px 0 32px;color:#5c5e62;font-size:14px;line-height:1.6}.consumer-auth form{display:grid;gap:18px}.auth-submit{width:100%;margin-top:4px}.switch{font-size:14px;color:#5c5e62;margin:4px 0 0}.switch a{color:#3e6ae1}@media(max-width:800px){.consumer-auth{grid-template-columns:1fr}.consumer-auth__scene{min-height:220px}.consumer-auth__scene div{left:24px;bottom:28px}.consumer-auth__scene p{font-size:26px}.consumer-auth__form-area{min-height:calc(100dvh - 220px);padding:28px 24px}.consumer-auth form{margin:58px 0 auto}}
</style>
