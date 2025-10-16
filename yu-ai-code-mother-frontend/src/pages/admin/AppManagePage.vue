<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import { listAppVoByPageAdmin, deleteAppByAdmin, updateAppByAdmin } from '@/service/api/appController.ts'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const searchParams = reactive<API.AppQueryRequest>({ pageNum: 1 })
const dataList = ref<API.AppVO[]>([])
const total = ref(0)
const loading = ref(false)

const doSearch = async () => {
  loading.value = true
  const res = await listAppVoByPageAdmin({ ...searchParams })
  loading.value = false
  if (res.data.code === 0) {
    dataList.value = res.data.data?.records || []
    total.value = res.data.data?.totalRow || 0
  } else {
    message.error('查询失败：' + res.data.message)
  }
}

const onPageChange = (page: number, pageSize: number) => {
  searchParams.pageNum = page
  searchParams.pageSize = pageSize
  doSearch()
}

const doDelete = async (id?: number) => {
  const res = await deleteAppByAdmin({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    doSearch()
  } else {
    message.error('删除失败：' + res.data.message)
  }
}

const doFeature = async (row: API.AppVO) => {
  const res = await updateAppByAdmin({ id: row.id, priority: 99 })
  if (res.data.code === 0) {
    message.success('已设为精选')
    doSearch()
  } else {
    message.error('设置失败：' + res.data.message)
  }
}

const toEdit = (row: API.AppVO) => {
  router.push(`/app/edit/${row.id}`)
}

onMounted(() => doSearch())
</script>

<template>
  <div style="padding: 24px">
    <a-card title="应用管理">
      <a-form layout="inline" @submit.prevent>
        <a-form-item label="名称">
          <a-input v-model:value="searchParams.appName" placeholder="按名称搜索" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="doSearch">搜索</a-button>
        </a-form-item>
      </a-form>
      <a-table :data-source="dataList" :loading="loading" row-key="id" :pagination="{ total, pageSize: searchParams.pageSize || 10, current: searchParams.pageNum || 1, onChange: onPageChange }">
        <a-table-column title="ID" data-index="id" key="id" />
        <a-table-column title="名称" data-index="appName" key="appName" />
        <a-table-column title="封面" key="cover" :customRender="({ record }) => record.cover ? h('img', { src: record.cover, style: 'height:40px' }) : '-'" />
        <a-table-column title="优先级" data-index="priority" key="priority" />
        <a-table-column title="操作" key="action" :customRender="({ record }) => (
          [
            h('a', { onClick: () => toEdit(record), style: 'margin-right:12px' }, '编辑'),
            h('a', { onClick: () => doDelete(record.id), style: 'margin-right:12px' }, '删除'),
            h('a', { onClick: () => doFeature(record) }, '精选'),
          ]
        )" />
      </a-table>
    </a-card>
  </div>
</template>
