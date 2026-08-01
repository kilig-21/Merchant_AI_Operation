<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const open = ref(false)
const close = () => { open.value = false }
const isShop = computed(() => route.path.startsWith('/stores') || route.path.startsWith('/products'))
const isOrders = computed(() => route.path.startsWith('/orders'))
</script>

<template>
  <header class="consumer-nav-wrap">
    <nav class="consumer-nav" aria-label="消费者端主导航">
      <RouterLink class="consumer-brand" to="/" @click="close">Morrow</RouterLink>
      <div class="consumer-nav__links" :class="{ 'is-open': open }">
        <RouterLink to="/" @click="close">首页</RouterLink>
        <RouterLink :class="{ active: isShop }" to="/stores/1/products" @click="close">选购</RouterLink>
        <RouterLink :class="{ active: isOrders }" to="/orders" @click="close">订单</RouterLink>
        <RouterLink to="/consumer/login" @click="close">账户</RouterLink>
        <RouterLink class="bag-link" to="/cart" @click="close" aria-label="购物袋">
          <span aria-hidden="true">◌</span><b>购物袋</b>
        </RouterLink>
      </div>
      <button class="nav-toggle" type="button" :aria-expanded="open" aria-label="Open navigation" @click="open = !open">Menu</button>
    </nav>
  </header>
</template>
