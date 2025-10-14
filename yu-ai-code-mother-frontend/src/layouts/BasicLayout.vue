<template>
  <a-layout class="basic-layout">
    <GlobalHeader/>
    <a-layout-content class="content">
      <div class="content-wrapper">
        <RouterView/>
      </div>
    </a-layout-content>
    <GlobalFooter/>
  </a-layout>
</template>

<script lang="ts" setup>
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
import {RouterView} from 'vue-router'
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.content {
  flex: 1;
  padding: 24px;
  background: #f0f2f5;
}

.content-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

@media (max-width: 768px) {
  .content {
    padding: 16px;
  }
}

@media (max-width: 576px) {
  .content {
    padding: 12px;
  }
}
</style>
