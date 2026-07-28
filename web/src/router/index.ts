import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import HomeView from '../views/HomeView.vue'
import ProductListView from '../views/merchant/ProductListView.vue'
import LoginView from '../views/merchant/LoginView.vue'
import ForbiddenView from '../views/ForbiddenView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: HomeView,
    },
    {
      path: '/merchant/login',
      component: LoginView,
    },
    {
      path: '/merchant/products',
      component: ProductListView,
      meta: {
        requiresMerchant: true,
      },
    },
    {
      path: '/403',
      component: ForbiddenView,
    },
  ],
})

router.beforeEach((to) => {
  const authStore = useAuthStore()

  if (to.meta.requiresMerchant && !authStore.token) {
    return {
      path: '/merchant/login',
      query: {
        redirect: to.fullPath,
      },
    }
  }

  if (to.meta.requiresMerchant && authStore.user && !authStore.isMerchant) {
    return {
      path: '/403',
    }
  }
})

export default router