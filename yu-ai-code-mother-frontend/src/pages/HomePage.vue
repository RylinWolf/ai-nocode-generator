<script lang="ts" setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { addApp, listMyAppVoByPage, listFeaturedAppVoByPage, deleteApp } from '@/service/api/appController.ts'
import type { API } from '@/service/api/typings'
import { message } from 'ant-design-vue'

const router = useRouter()

// create app
const prompt = ref('')
const creating = ref(false)
const doCreate = async () => {
  const text = prompt.value.trim()
  if (!text) return message.warning('请输入提示词')
  creating.value = true
  const res = await addApp({ initPrompt: text })
  creating.value = false
  if (res.data.code === 0) {
    const id = res.data.data as number
    message.success('创建成功，进入对话生成页面')
    router.push(`/app/chat/${id}`)
  } else {
    message.error('创建失败：' + res.data.message)
  }
}

// my apps list
const myQuery = reactive<API.AppQueryRequest>({ pageNum: 1, pageSize: 20 })
const myList = ref<API.AppVO[]>([])
const myTotal = ref(0)
const loadingMy = ref(false)
const loadMy = async () => {
  loadingMy.value = true
  const res = await listMyAppVoByPage({ ...myQuery })
  loadingMy.value = false
  if (res.data.code === 0) {
    myList.value = res.data.data?.records || []
    myTotal.value = res.data.data?.totalRow || 0
  }
}
const onMyPage = (page: number) => {
  myQuery.pageNum = page
  loadMy()
}
const removeMy = async (id?: number) => {
  const res = await deleteApp({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    loadMy()
  } else message.error(res.data.message)
}

// featured list
const featQuery = reactive<API.AppQueryRequest>({ pageNum: 1, pageSize: 20 })
const featList = ref<API.AppVO[]>([])
const featTotal = ref(0)
const loadingFeat = ref(false)
const loadFeat = async () => {
  loadingFeat.value = true
  const res = await listFeaturedAppVoByPage({ ...featQuery })
  loadingFeat.value = false
  if (res.data.code === 0) {
    featList.value = res.data.data?.records || []
    featTotal.value = res.data.data?.totalRow || 0
  }
}
const onFeatPage = (page: number) => {
  featQuery.pageNum = page
  loadFeat()
}

loadMy()
loadFeat()
</script>

<template>
  <div>
    <div style="text-align:center; padding: 48px 12px 24px;">
      <h1>一句话 造所想</h1>
      <div style="max-width:1000px;margin:0 auto;background:#fff;border-radius:10px;border:1px solid #f0f0f0;padding:16px">
        <a-input-textarea v-model:value="prompt" :rows="3" placeholder="使用 NoCode 创建一个高效的小工具，帮我计算……" />
        <div style="text-align:right; margin-top:8px">
          <a-button type="primary" :loading="creating" @click="doCreate">开始创建</a-button>
        </div>
      </div>
    </div>

    <div style="max-width:1200px;margin:24px auto">
      <h2>我的作品</h2>
      <a-input-search v-model:value="myQuery.appName" placeholder="按名称搜索" style="max-width:320px;margin:8px 0" @search="loadMy" />
      <a-list :loading="loadingMy" :grid="{ gutter: 16, column: 4 }" :data-source="myList">
        <template #renderItem="{ item }">
          <a-list-item>
            <a-card :hoverable="true" @click="$router.push(`/app/chat/${item.id}`)">
              <div v-if="item.cover" style="height:140px;overflow:hidden;text-align:center;background:#fafafa;margin-bottom:8px">
                <img :src="item.cover" style="max-height:140px;max-width:100%"/>
              </div>
              <a-card-meta :title="item.appName" />
              <template #actions>
                <a @click.stop="$router.push(`/app/edit/${item.id}`)">编辑</a>
                <a @click.stop="removeMy(item.id)">删除</a>
              </template>
            </a-card>
          </a-list-item>
        </template>
      </a-list>
      <a-pagination :current="myQuery.pageNum || 1" :total="myTotal" :pageSize="myQuery.pageSize || 20" @change="onMyPage" style="margin-top:12px;text-align:right" />
    </div>

    <div style="max-width:1200px;margin:24px auto">
      <h2>精选案例</h2>
      <a-input-search v-model:value="featQuery.appName" placeholder="按名称搜索" style="max-width:320px;margin:8px 0" @search="loadFeat" />
      <a-list :loading="loadingFeat" :grid="{ gutter: 16, column: 4 }" :data-source="featList">
        <template #renderItem="{ item }">
          <a-list-item>
            <a-card :hoverable="true" @click="$router.push(`/app/chat/${item.id}`)" :cover="item.cover ? $h('img', { src: item.cover }) : undefined">
              <a-card-meta :title="item.appName" />
            </a-card>
          </a-list-item>
        </template>
      </a-list>
      <a-pagination :current="featQuery.pageNum || 1" :total="featTotal" :pageSize="featQuery.pageSize || 20" @change="onFeatPage" style="margin-top:12px;text-align:right" />
    </div>
  </div>
</template>
