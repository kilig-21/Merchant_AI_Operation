<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

defineProps<{ title: string; eyebrow?: string }>()
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const menuOpen = ref(false)

const nav = [
  { to: '/merchant/dashboard', label: '经营概览', index: '01' },
  { to: '/merchant/products', label: '商品管理', index: '02' },
  { to: '/merchant/orders', label: '订单管理', index: '03' },
  { to: '/merchant/settings', label: '店铺设置', index: '04' },
]
const current = computed(() => nav.find((item) => route.path.startsWith(item.to))?.label ?? '商家工作台')

function signOut() {
  auth.signOut()
  router.push('/merchant/login')
}
</script>

<template>
  <main class="merchant-shell">
    <aside class="merchant-sidebar" :class="{ 'is-open': menuOpen }">
      <div class="merchant-sidebar__top">
        <RouterLink class="merchant-logo" to="/merchant/dashboard">Morrow<span>OS</span></RouterLink>
        <button class="merchant-menu-close" type="button" @click="menuOpen = false">关闭</button>
      </div>
      <p class="meta-label">MERCHANT WORKSPACE</p>
      <nav aria-label="商家工作台导航">
        <RouterLink v-for="item in nav" :key="item.to" :to="item.to" @click="menuOpen = false">
          <span>{{ item.index }}</span>{{ item.label }}<b>↗</b>
        </RouterLink>
      </nav>
      <div class="merchant-sidebar__account">
        <span>{{ auth.user?.username || 'merchant admin' }}</span>
        <small>{{ auth.user?.tenantId ? `Tenant ${auth.user.tenantId}` : '商家账户' }}</small>
        <button type="button" @click="signOut">退出登录</button>
      </div>
    </aside>

    <section class="merchant-workspace">
      <header class="merchant-mobile-bar">
        <button type="button" @click="menuOpen = true">菜单</button>
        <span>{{ current }}</span>
      </header>
      <header class="merchant-page-head">
        <div>
          <p class="meta-label">{{ eyebrow || 'MORROW OPERATIONS' }}</p>
          <h1>{{ title }}</h1>
        </div>
        <slot name="actions" />
      </header>
      <slot />
    </section>
  </main>
</template>

