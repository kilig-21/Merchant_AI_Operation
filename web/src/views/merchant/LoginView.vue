<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const username = ref('merchant_a_admin')
const password = ref('123456')
const errorMessage = ref('')

async function submitLogin() {
  errorMessage.value = ''

  try {
    await authStore.signIn({
      username: username.value,
      password: password.value,
    })

    const redirect = typeof route.query.redirect === 'string'
      ? route.query.redirect
      : '/merchant/products'

    router.push(redirect)
  } catch (error) {
    errorMessage.value = '登录失败，请确认后端已启动，或检查账号密码'
  }
}
</script>

<template>
  <main class="page">
    <h1>商家登录</h1>

    <form class="login-form" @submit.prevent="submitLogin">
      <label>
        账号
        <input v-model="username" />
      </label>

      <label>
        密码
        <input v-model="password" type="password" />
      </label>

      <button type="submit">登录</button>

      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
    </form>
  </main>
</template>

<style scoped>
.page {
  padding: 32px;
  font-family: Arial, sans-serif;
}

.login-form {
  display: grid;
  gap: 16px;
  width: 320px;
}

label {
  display: grid;
  gap: 6px;
}

input {
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

.error {
  color: #c00;
}
</style>