<template>
  <DevelopingPage
    title="个人设置"
    description="设置页面已创建，当前处于开发阶段。后续会补充账号安全、通知偏好和界面个性化设置。"
    :todo-list="['账号与隐私配置', '通知与消息偏好', '主题与显示设置']"
  />
</template>

<script setup lang="ts" name="SettingsView">
import DevelopingPage from '@/components/common/DevelopingPage.vue'
</script>
