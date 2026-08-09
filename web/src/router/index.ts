import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import HomeView from '../views/HomeView.vue'
import ForbiddenView from '../views/ForbiddenView.vue'
import NotFoundView from '../views/NotFoundView.vue'
import StoreProductsView from '../views/consumer/StoreProductsView.vue'
import ProductDetailView from '../views/consumer/ProductDetailView.vue'
import AuthView from '../views/consumer/AuthView.vue'
import CartView from '../views/consumer/CartView.vue'
import OrderListView from '../views/consumer/OrderListView.vue'
import OrderDetailView from '../views/consumer/OrderDetailView.vue'
import AccountView from '../views/consumer/AccountView.vue'
import MerchantLoginView from '../views/merchant/LoginView.vue'
import MerchantDashboardView from '../views/merchant/DashboardView.vue'
import MerchantProductListView from '../views/merchant/ProductListView.vue'
import MerchantProductCreateView from '../views/merchant/ProductCreateView.vue'
import MerchantOrdersView from '../views/merchant/OrdersView.vue'
import MerchantSettingsView from '../views/merchant/SettingsView.vue'

const router = createRouter({
  history: createWebHistory(),
  scrollBehavior: () => ({ top: 0 }),
  routes: [
    { path: '/', component: HomeView },
    { path: '/stores/:storeId/products', component: StoreProductsView },
    { path: '/stores/:storeId/products/:spuId', component: ProductDetailView },
    { path: '/products/:spuId', redirect: (to) => `/stores/1001/products/${to.params.spuId}` },
    { path: '/consumer/login', component: AuthView, props: { mode: 'login' } },
    { path: '/consumer/register', component: AuthView, props: { mode: 'register' } },
    { path: '/cart', component: CartView, meta: { requiresConsumer: true } },
    { path: '/orders', component: OrderListView, meta: { requiresConsumer: true } },
    { path: '/orders/:id', component: OrderDetailView, meta: { requiresConsumer: true } },
    { path: '/account', component: AccountView, meta: { requiresConsumer: true } },
    { path: '/merchant/login', component: MerchantLoginView },
    { path: '/merchant/dashboard', component: MerchantDashboardView, meta: { requiresMerchant: true } },
    { path: '/merchant/products', component: MerchantProductListView, meta: { requiresMerchant: true } },
    { path: '/merchant/products/new', component: MerchantProductCreateView, meta: { requiresMerchant: true } },
    { path: '/merchant/orders', component: MerchantOrdersView, meta: { requiresMerchant: true } },
    { path: '/merchant/settings', component: MerchantSettingsView, meta: { requiresMerchant: true } },
    { path: '/403', component: ForbiddenView },
    { path: '/:pathMatch(.*)*', component: NotFoundView },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresMerchant && !auth.token) return { path: '/merchant/login', query: { redirect: to.fullPath } }
  if (to.meta.requiresMerchant && auth.user && !auth.isMerchant) return { path: '/403' }
  if (to.meta.requiresConsumer && !auth.token) return { path: '/consumer/login', query: { redirect: to.fullPath } }
  if (to.meta.requiresConsumer && auth.user && auth.isMerchant) return { path: '/403' }
})

export default router
