<template>
  <DevelopingPage
    title="关于本站"
    description="关于本站页面已创建，当前处于开发阶段。后续会补充建站初衷、技术架构与版本迭代记录。"
    :todo-list="['站点介绍', '技术架构说明', '版本更新记录']"
  />
</template>

<script setup lang="ts" name="AboutSiteView">
import DevelopingPage from '@/components/common/DevelopingPage.vue'
</script>
