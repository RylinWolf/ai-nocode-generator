<template>
  <a-layout-footer class="footer">
    <div class="footer-content">
      原创 程序员鱼皮，重构 by
      <a href="https://wolfblog.cn" rel="noopener noreferrer" target="_blank">Rylin</a>
    </div>
  </a-layout-footer>
</template>

<script lang="ts" setup></script>

<style scoped>
.footer {
  background: #f0f2f5;
  text-align: center;
  padding: 24px 50px;
  position: sticky;
  bottom: 0;
  width: 100%;
}

.footer-content {
  color: rgba(0, 0, 0, 0.65);
  font-size: 14px;
}

.footer-content a {
  color: #1890ff;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-content a:hover {
  color: #40a9ff;
  text-decoration: underline;
}
</style>
