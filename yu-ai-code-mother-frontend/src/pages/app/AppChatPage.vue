<script lang="ts" setup>
import { onMounted, reactive, ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { message as antdMessage } from 'ant-design-vue'
import { getAppVoById, chatToGenCode, deployApp } from '@/service/api/appController.ts'

const route = useRoute()
const appId = Number(route.params.id)

const app = ref<API.AppVO>({})
const loading = ref(false)

// messages
interface Msg { role: 'user' | 'ai'; content: string }
const messages = ref<Msg[]>([])
const input = ref('')

const iframeUrl = computed(() => {
  if (!app.value?.codeGenType || !app.value?.id) return ''
  return `http://localhost:8123/api/static/${app.value.codeGenType}_${app.value.id}/`
})

const siteReady = ref(false)

const fetchApp = async () => {
  const res = await getAppVoById({ id: appId })
  if (res.data.code === 0) {
    app.value = res.data.data || {}
  } else {
    antdMessage.error('获取应用失败：' + res.data.message)
  }
}

const send = async (text?: string) => {
  const msg = (text ?? input.value).trim()
  if (!msg) return
  messages.value.push({ role: 'user', content: msg })
  input.value = ''
  // SSE via EventSource
  loading.value = true
  try {
    const url = new URL('/api/app/chat/gen/code', 'http://localhost:8123')
    url.searchParams.set('appId', String(appId))
    url.searchParams.set('message', msg)
    const es = new EventSource(url.toString(), { withCredentials: true } as any)
    let aiBuffer = ''
    es.onmessage = (ev) => {
      const data = ev.data
      if (data === '[DONE]') {
        es.close()
        loading.value = false
        messages.value.push({ role: 'ai', content: aiBuffer })
        // site becomes ready after stream complete
        siteReady.value = true
        return
      }
      aiBuffer += data
    }
    es.onerror = () => {
      es.close()
      loading.value = false
    }
  } catch (e) {
    loading.value = false
    console.error(e)
    antdMessage.error('对话失败')
  }
}

const doDeploy = async () => {
  const res = await deployApp({ appId })
  if (res.data.code === 0) {
    antdMessage.success('部署成功，访问地址：' + res.data.data)
    window.open(res.data.data as string, '_blank')
  } else {
    antdMessage.error('部署失败：' + res.data.message)
  }
}

onMounted(async () => {
  await fetchApp()
  // auto send initial prompt if exists and first enter
  if (app.value?.initPrompt) {
    await send(app.value.initPrompt)
  }
})
</script>

<template>
  <a-layout style="height: calc(100vh - 64px)">
    <a-layout-header style="background:#fff;border-bottom:1px solid #f0f0f0">
      <a-space style="width:100%;justify-content:space-between">
        <div>{{ app.appName || '应用对话' }}</div>
        <a-button type="primary" @click="doDeploy">部署</a-button>
      </a-space>
    </a-layout-header>
    <a-layout>
      <a-layout-content style="padding:12px">
        <a-row :gutter="12">
          <a-col :span="12">
            <div style="height: calc(100vh - 64px - 64px - 24px); display:flex; flex-direction:column">
              <div style="flex:1; overflow:auto; border:1px solid #f0f0f0; border-radius:4px; padding:12px">
                <div v-for="(m, idx) in messages" :key="idx" :style="{ textAlign: m.role === 'user' ? 'right' : 'left', marginBottom: '8px' }">
                  <a-tag :color="m.role==='user'?'blue':'green'">{{ m.role==='user'?'我':'AI' }}</a-tag>
                  <span style="white-space:pre-wrap">{{ m.content }}</span>
                </div>
                <a-empty v-if="messages.length===0" description="开始对话以生成站点"/>
              </div>
              <div style="margin-top:8px; display:flex; gap:8px">
                <a-input-textarea v-model:value="input" :rows="2" placeholder="描述要生成的网站..." />
                <a-button type="primary" :loading="loading" @click="send">发送</a-button>
              </div>
            </div>
          </a-col>
          <a-col :span="12">
            <div style="height: calc(100vh - 64px - 64px - 24px); border:1px solid #f0f0f0; border-radius:4px; overflow:hidden">
              <template v-if="siteReady && iframeUrl">
                <iframe :src="iframeUrl" style="border:0;width:100%;height:100%" />
              </template>
              <a-empty v-else description="生成完成后将在此预览" style="margin-top:120px" />
            </div>
          </a-col>
        </a-row>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>
