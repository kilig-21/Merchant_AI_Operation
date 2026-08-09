<script setup lang="ts">
const props = defineProps<{ status: string }>()

const labels: Record<string, string> = {
  ON_SALE: '在售',
  OFF_SALE: '已下架',
  DRAFT: '草稿',
  PENDING_PAYMENT: '待支付',
  PAID: '已支付',
  CLOSED: '已关闭',
}
</script>

<template>
  <span class="status-pill" :class="`status-pill--${props.status.toLowerCase()}`">
    <i aria-hidden="true" />{{ labels[props.status] ?? props.status }}
  </span>
</template>

