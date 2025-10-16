<script lang="ts" setup>
import { onMounted, reactive, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAppVoById, updateApp, updateAppByAdmin } from '@/service/api/appController.ts'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'

const route = useRoute()
const router = useRouter()
const id = Number(route.params.id)

const loginUserStore = useLoginUserStore()
const isAdmin = computed(() => loginUserStore.loginUser?.userRole === 'admin')

const form = reactive<{ appName?: string; cover?: string; priority?: number }>({})
const loading = ref(false)

const load = async () => {
  const res = await getAppVoById({ id })
  if (res.data.code === 0 && res.data.data) {
    const a = res.data.data
    form.appName = a.appName
    form.cover = a.cover
    form.priority = a.priority
  }
}

const onSubmit = async () => {
  loading.value = true
  try {
    if (isAdmin.value) {
      const res = await updateAppByAdmin({ id, appName: form.appName, cover: form.cover, priority: form.priority })
      if (res.data.code === 0) {
        message.success('保存成功')
        router.back()
      } else message.error(res.data.message)
    } else {
      const res = await updateApp({ id, appName: form.appName })
      if (res.data.code === 0) {
        message.success('保存成功')
        router.back()
      } else message.error(res.data.message)
    }
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div style="padding:24px">
    <a-card :title="`编辑应用 #${id}`">
      <a-form :model="form" label-col="{ span: 4 }" wrapper-col="{ span: 14 }" @submit.prevent>
        <a-form-item label="名称">
          <a-input v-model:value="form.appName" placeholder="应用名称" />
        </a-form-item>
        <template v-if="isAdmin">
          <a-form-item label="封面">
            <a-input v-model:value="form.cover" placeholder="封面 URL" />
          </a-form-item>
          <a-form-item label="优先级">
            <a-input-number v-model:value="form.priority" :min="0" />
          </a-form-item>
        </template>
        <a-form-item>
          <a-button type="primary" :loading="loading" @click="onSubmit">保存</a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>
