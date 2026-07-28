<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getMerchantProducts, type MerchantProductItem } from '../../api/product'

const products = ref<MerchantProductItem[]>([])
const keyword = ref('')
const loading = ref(false)
const errorMessage = ref('')

async function loadProducts() {
  loading.value = true
  errorMessage.value = ''

  try {
    products.value = await getMerchantProducts({
      page: 1,
      size: 10,
      keyword: keyword.value || undefined,
    })
  } catch (error) {
    errorMessage.value = '商品列表加载失败，请确认后端已启动并且已登录'
  } finally {
    loading.value = false
  }
}

function statusText(status: MerchantProductItem['status']) {
  if (status === 'ON_SALE') {
    return '上架中'
  }

  if (status === 'OFF_SALE') {
    return '已下架'
  }

  return '草稿'
}

onMounted(() => {
  loadProducts()
})
</script>

<template>
  <main class="page">
    <h1>商家商品管理</h1>

    <section class="toolbar">
      <input v-model="keyword" placeholder="输入商品名称搜索" />
      <button @click="loadProducts">搜索</button>
    </section>

    <p v-if="loading">加载中...</p>
    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <table v-if="!loading && products.length > 0">
      <thead>
        <tr>
          <th>商品名称</th>
          <th>状态</th>
          <th>SKU 数</th>
          <th>最低价</th>
          <th>总库存</th>
        </tr>
      </thead>

      <tbody>
        <tr v-for="product in products" :key="product.id">
          <td>{{ product.name }}</td>
          <td>{{ statusText(product.status) }}</td>
          <td>{{ product.skuCount }}</td>
          <td>{{ product.minSalePrice ?? '-' }}</td>
          <td>{{ product.totalAvailableStock }}</td>
        </tr>
      </tbody>
    </table>

    <p v-if="!loading && products.length === 0">暂无商品</p>
  </main>
</template>

<style scoped>
.page {
  padding: 32px;
  font-family: Arial, sans-serif;
}

.toolbar {
  display: flex;
  gap: 12px;
  margin: 24px 0;
}

input {
  width: 260px;
  padding: 8px 12px;
  border: 1px solid #222;
  font-size: 16px;
}

button {
  padding: 8px 14px;
  border: 1px solid #222;
  background: white;
  cursor: pointer;
}

table {
  width: 760px;
  margin-top: 16px;
  border-collapse: collapse;
}

th,
td {
  padding: 10px 12px;
  border: 1px solid #ddd;
  text-align: left;
}

th {
  background: #f6f6f6;
}

.error {
  color: #c00;
}
</style>