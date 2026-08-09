<script setup lang="ts">
import { useRouter } from 'vue-router'
import ConsumerNav from '../../components/ConsumerNav.vue'
import EditorialFooter from '../../components/EditorialFooter.vue'
import { useAuthStore } from '../../stores/auth'
const auth = useAuthStore(); const router = useRouter()
function signOut(){ auth.signOut(); router.push('/') }
</script>

<template>
  <div><ConsumerNav/><main class="account-shell">
    <header><p class="meta-label">MEMBER / {{ auth.user?.id || '—' }}</p><h1>你好，<br>{{ auth.user?.username || 'Morrow member' }}。</h1><p>订单、账户和售后入口，都收在这个安静的角落。</p></header>
    <section class="account-grid">
      <RouterLink to="/orders" class="account-card account-card--accent"><span>01</span><div><p>真实业务</p><h2>我的订单</h2><small>查看状态、支付或取消待支付订单</small></div><b>↗</b></RouterLink>
      <article class="account-card"><span>02</span><div><p>静态预览</p><h2>收货信息</h2><small>后端地址簿尚未接入，此处仅作页面骨架</small></div><b>—</b></article>
      <article class="account-card"><span>03</span><div><p>静态预览</p><h2>喜欢清单</h2><small>收藏能力尚未接入，先保留未来入口</small></div><b>—</b></article>
      <article class="account-card"><span>04</span><div><p>账户</p><h2>安全与退出</h2><small>当前登录身份：{{ auth.user?.userType || 'CONSUMER' }}</small></div><button type="button" @click="signOut">退出</button></article>
    </section>
  </main><EditorialFooter/></div>
</template>

<style scoped>
.account-shell{width:min(1240px,calc(100% - 64px));margin:auto;padding:clamp(120px,15vw,210px) 0 120px}header{display:grid;grid-template-columns:1fr 330px;align-items:end;gap:40px;margin-bottom:80px}h1{margin:18px 0 0;font:500 clamp(58px,8vw,112px)/.88 var(--font-display);letter-spacing:-.065em}header>p:last-child{color:var(--muted);line-height:1.7}.account-grid{display:grid;grid-template-columns:1fr 1fr;border-top:1px solid var(--line)}.account-card{min-height:240px;display:grid;grid-template-columns:36px 1fr auto;gap:24px;padding:30px;border-right:1px solid var(--line);border-bottom:1px solid var(--line);transition:background .25s,transform .25s}.account-card:nth-child(2n){border-right:0}.account-card:hover{background:var(--soft);transform:translateY(-3px)}.account-card--accent{background:var(--signal)}.account-card span,.account-card p,.account-card small{font-size:12px}.account-card p{margin:0 0 46px;color:var(--muted);text-transform:uppercase;letter-spacing:.1em}.account-card h2{margin:0 0 10px;font:500 32px/1 var(--font-display)}.account-card small{color:var(--muted);line-height:1.55}.account-card b{font-weight:400}.account-card button{align-self:start;border:0;background:none;text-decoration:underline}@media(max-width:720px){.account-shell{width:calc(100% - 40px);padding-top:110px}header{grid-template-columns:1fr;margin-bottom:50px}.account-grid{grid-template-columns:1fr}.account-card,.account-card:nth-child(2n){border-right:0}}
</style>
