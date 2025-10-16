import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '@/pages/HomePage.vue'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue'
import UserManagePage from '@/pages/admin/UserManagePage.vue'
import AppManagePage from '@/pages/admin/AppManagePage.vue'
import AppChatPage from '@/pages/app/AppChatPage.vue'
import AppEditPage from '@/pages/app/AppEditPage.vue'
import ACCESS_ENUM from '@/access/accessEnum.ts'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '主页',
      component: HomePage,
    },
    {
      path: '/user/login',
      name: '用户登录',
      component: UserLoginPage,
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: UserRegisterPage,
    },
    {
      path: '/admin/userManage',
      name: 'adminUserManage',
      component: UserManagePage,
      meta: {
        access: ACCESS_ENUM.ADMIN,
      },
    },
    {
      path: '/admin/appManage',
      name: 'adminAppManage',
      component: AppManagePage,
      meta: {
        access: ACCESS_ENUM.ADMIN,
      },
    },
    {
      path: '/app/chat/:id',
      name: 'appChat',
      component: AppChatPage,
    },
    {
      path: '/app/edit/:id',
      name: 'appEdit',
      component: AppEditPage,
    },
  ],
})

export default router
