<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const auth = useAuthStore()
const open = ref(false)
const close = () => { open.value = false }
const isShop = computed(() => route.path.startsWith('/stores') || route.path.startsWith('/products'))
const isOrders = computed(() => route.path.startsWith('/orders'))
const accountTarget = computed(() => auth.isLoggedIn ? '/account' : '/consumer/login')
</script>

<template>
  <header class="consumer-nav-wrap">
    <nav class="consumer-nav" aria-label="消费者端主导航">
      <RouterLink class="consumer-brand" to="/" @click="close">Morrow<sup>©26</sup></RouterLink>
      <div class="consumer-nav__links" :class="{ 'is-open': open }">
        <RouterLink to="/" @click="close">首页</RouterLink>
        <RouterLink :class="{ active: isShop }" to="/stores/1001/products" @click="close">选购</RouterLink>
        <RouterLink :class="{ active: isOrders }" to="/orders" @click="close">订单</RouterLink>
        <RouterLink to="/account" @click="close">服务</RouterLink>
      </div>
      <div class="consumer-nav__actions">
        <RouterLink class="account-link" :to="accountTarget">{{ auth.isLoggedIn ? '账户' : '登录' }}</RouterLink>
        <RouterLink class="bag-link" to="/cart" aria-label="购物袋"><span>购物袋</span><i aria-hidden="true">↗</i></RouterLink>
      </div>
      <button class="nav-toggle" type="button" :aria-expanded="open" aria-label="打开导航" @click="open = !open">{{ open ? '关闭' : '菜单' }}</button>
    </nav>
  </header>
</template>
