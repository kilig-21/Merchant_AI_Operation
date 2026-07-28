<script setup lang="ts">

import { computed, ref } from 'vue'

const merchantName = ref('成都示例商家')
const clickCount = ref(0)
const keyword = ref('')

// 商品列表数据
const products = ref([
  { id: 1, name: '蓝牙耳机', stock: 70 },
  { id: 2, name: '机械键盘', stock: 24 },
  { id: 3, name: '便携充电宝', stock: 0 },
  { id: 4, name: '智能手表', stock: 15},
])

// 计算属性：根据关键字过滤商品列表
const filteredProducts = computed(() => {
  if (!keyword.value) {
    return products.value
  }

  return products.value.filter((product) =>
    product.name.includes(keyword.value),
  )
})
// 点击事件处理函数(数字自增)
function increaseCount() {
  clickCount.value = clickCount.value + 1
}

function addProduct() {
  products.value.push({
    id: Date.now(),
    name: '测试商品',
    stock: 10,
  })
}

function decreaseStock(productId: number) {
  const product = products.value.find((item) => item.id === productId)
  if (!product || product.stock === 0) {
    return
  }

  product.stock = product.stock - 1
}

</script>

<template>
  <main class="page">
    <h1>AI 智能经营电商平台</h1>

    <section class="block">
      <h2>变量显示</h2>
      <P>当前商家:{{ merchantName }}</P>
    </section>

    <section class="block">
      <h2>点击事件</h2>
      <button @click="increaseCount">点击次数：{{ clickCount }}</button>
    </section>

 <section class="block">
  <h2>列表渲染</h2>

  <input v-model="keyword" placeholder="输入商品名称搜索" />
  <button @click="addProduct">新增测试商品</button>

  <table>
    <thead>
      <tr>
        <th>商品名称</th>
        <th>库存</th>
        <th>状态</th>
        <th>操作</th>
      </tr>
    </thead>

 <tbody>
  <tr v-for="product in filteredProducts" :key="product.id">
    <td>{{ product.name }}</td>
    <td>{{ product.stock }}</td>
    <td>
      <span v-if="product.stock === 0">售罄</span>
      <span v-else>可售</span>
    </td>
    <td>
      <button
        :disabled="product.stock === 0"
        @click="decreaseStock(product.id)"
      >
        减库存
      </button>
    </td>
  </tr>
</tbody>
</table>

  <p v-if="filteredProducts.length === 0">没有找到商品</p>
</section>
  </main>
</template>

<style scoped>
.page {
  padding: 32px;
  font-family: Arial, sans-serif;
}

.block {
  margin-top: 24px;
}

button {
  padding: 8px 14px;
  border: 1px solid #222;
  background: white;
  cursor: pointer;
}

input {
  width: 240px;
  padding: 8px 12px;
  margin-right: 12px;
  border: 1px solid #222;
  font-size: 16px;
}
table {
  width: 520px;
  margin-top: 16px;
  border-collapse: collapse;
}

th , td {
  padding: 10px 12px;
  border: 1px solid #ddd;
  text-align: left;
}

th {
  background: #f6f6f6;
}
</style>