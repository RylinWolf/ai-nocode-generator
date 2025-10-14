<template>
  <a-layout-header class="header">
    <a-flex align="center" class="header-content" justify="space-between">
      <a-space :size="16" align="center" class="left-section">
        <img alt="logo" class="logo" src="@/assets/logo.png"/>
        <a-typography-title :level="4" class="title">
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
        <a-button type="primary">{{ LOGIN_TEXT }}</a-button>
      </a-space>
    </a-flex>
  </a-layout-header>
</template>

<script lang="ts" setup>
import {ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import type {MenuProps} from 'ant-design-vue'

// 常量定义
const APP_TITLE = 'AI 代码生成器'
const LOGIN_TEXT = '登录'

const router = useRouter()
const route = useRoute()

const selectedKeys = ref<string[]>(['home'])
// 监听路由变化
router.afterEach((to) => {
  selectedKeys.value = [(to.name as string) || 'home']
})

const menuItems: MenuProps['items'] = [
  {
    key: 'home',
    label: '主页',
  },
  {
    key: 'about',
    label: '关于',
  },
]

const handleMenuClick = ({key}: { key: string }) => {
  selectedKeys.value = [key]
  router.push({name: key})
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
  flex: 1;
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
  flex: 1;
  border-bottom: none;
  line-height: 64px;
  min-width: 0;
}

@media (max-width: 768px) {
  .title {
    font-size: 16px;
  }

  .menu {
    display: none;
  }
}
</style>
