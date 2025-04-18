import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from "@/views/DashboardView.vue";
import EditTimeSheetView from "@/views/EditTimeSheetView.vue" ;
import UserProfileView from "@/views/UserProfileView.vue";
import { useAuthStore } from '@/stores/auth';
import { authGuard } from './authGuard';

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
    },
    {
      path: '/access-denied',
      name: 'access-denied',
      component: () => import('@/views/AccessDeniedView.vue')
    },
    {
      path: '/groups',
      name: 'groups',
      component: () => import('@/views/GroupsView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/mobile',
      name: 'mobile',
      component: () => import('@/views/MobileView.vue')
    },
    {
      path: '/profile',
      name: 'profile',
      component: UserProfileView,
      meta: { requiresAuth: true }
    }
  ],
})

router.beforeEach(authGuard);

export default router
