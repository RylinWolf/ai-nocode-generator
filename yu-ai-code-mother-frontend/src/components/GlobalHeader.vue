<template>
  <a-layout-header class="header">
    <a-flex align="center" class="header-content" justify="space-between">
      <a-space :size="24" align="center" class="left-section">
        <img alt="logo" class="logo" src="@/assets/logo.png" />
        <a-typography-title :level="3" class="title">
          {{ APP_TITLE }}
        </a-typography-title>
        <a-menu
          v-model:selectedKeys="selectedKeys"
          :items="menuItems"
          class="menu"
          mode="horizontal"
          @click="handleMenuClick"
        />
      </a-space>
      <a-space class="right-section">
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-space>
                <a-avatar :src="loginUserStore.loginUser.userAvatar" />
                {{ loginUserStore.loginUser.userName ?? '无名' }}
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button href="/user/login" type="primary">登录</a-button>
            <a-button href="/user/register" type="primary">注册</a-button>
          </div>
        </div>
      </a-space>
    </a-flex>
  </a-layout-header>
</template>

<script lang="ts" setup>
import { computed, h, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { type MenuProps, message } from 'ant-design-vue'
import { LogoutOutlined, HomeOutlined } from '@ant-design/icons-vue'

// JS 中引入 Store
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { login, logout } from '@/service/api/userController.ts'
import checkAccess from '@/access/checkAccess.ts'
const loginUserStore = useLoginUserStore()

// 常量定义
const APP_TITLE = 'AI 代码生成器'

const router = useRouter()
const route = useRoute()

const selectedKeys = ref<string[]>(['/'])
// 监听路由变化
router.afterEach((to) => {
  selectedKeys.value = [(to.path as string) || '/']
})

// 菜单配置项
const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: 'others',
    label: h('a', { href: 'https://www.codefather.cn', target: '_blank' }, '编程导航'),
    title: '编程导航',
  },
]

// 过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  // 过滤菜单项
  return menus?.filter((menu) => {
    const menuKey = menu?.key as string
    if (menuKey?.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

// 展示在菜单的路由数组
const menuItems = computed<MenuProps['items']>(() => filterMenus(originItems))

const handleMenuClick = ({ key }: { key: string }) => {
  selectedKeys.value = [key]
  router.push({ path: key })
}

// 用户注销
const doLogout = async () => {
  const res = await logout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 999;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  height: 64px;
}

.left-section {
  width: auto;
}

.logo {
  height: 40px;
  width: auto;
}

.title {
  color: #1890ff;
  margin: 0 !important;
  white-space: nowrap;
}

.menu {
  flex-grow: 1;
  border-bottom: none;
  line-height: 64px;
  width: auto;
}

@media (max-width: 768px) {
  .title {
    font-size: 16px;
  }
}
</style>
