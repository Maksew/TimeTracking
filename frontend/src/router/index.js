import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from "@/views/DashboardView.vue";
import EditTimeSheetView from "@/views/EditTimeSheetView.vue" ;
import { useAuthStore } from '@/stores/auth';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'dashboard',
      component: DashboardView,
      meta: { requiresAuth: true }
    },
    {
      path: '/timesheet',
      name: 'timesheet',
      component: () => import('@/views/TimeSheetView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/editTimesheet',
      name: 'editTimesheet',
      component: EditTimeSheetView,
      meta: { requiresAuth: true }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue')
    }
  ],
})

router.beforeEach((to, from,
                   next) => {
  const authStore = useAuthStore();

  // Initialise l'auth à partir du localStorage au premier chargement
  if (!authStore.isAuthenticated) {
    authStore.initAuth();
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login');
  } else {
    next();
  }
})

export default router
