<template>
  <div class="p-4 bg-white dark:bg-[#0f172a] rounded-lg shadow-sm transition-colors duration-300">
    <h2 class="text-2xl font-bold mb-4 text-gray-800 dark:text-white">网站设置</h2>

    <el-alert
      title="用于控制后台功能入口的可见性和可访问性。关闭后，菜单会隐藏，直接访问也会被拦截。"
      type="info"
      :closable="false"
      class="mb-4"
    />

    <el-form label-width="140px">
      <el-form-item label="用户管理页面">
        <el-switch
          v-model="siteSettings.services.userManagementEnabled"
          inline-prompt
          active-text="开启"
          inactive-text="关闭"
        />
      </el-form-item>
      <el-form-item label="统计服务">
        <el-switch
          v-model="siteSettings.services.statsServiceEnabled"
          inline-prompt
          active-text="开启"
          inactive-text="关闭"
        />
      </el-form-item>
      <el-form-item label="互动服务">
        <el-switch
          v-model="siteSettings.services.interactionServiceEnabled"
          inline-prompt
          active-text="开启"
          inactive-text="关闭"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="saveServices">保存服务配置</el-button>
      </el-form-item>
    </el-form>

    <div class="grid gap-3 md:grid-cols-3 mt-6">
      <div class="service-card">
        <span>用户管理</span>
        <el-tag :type="siteSettings.services.userManagementEnabled ? 'success' : 'info'">
          {{ siteSettings.services.userManagementEnabled ? '已开启' : '已关闭' }}
        </el-tag>
      </div>
      <div class="service-card">
        <span>统计服务</span>
        <el-tag :type="siteSettings.services.statsServiceEnabled ? 'success' : 'info'">
          {{ siteSettings.services.statsServiceEnabled ? '已开启' : '已关闭' }}
        </el-tag>
      </div>
      <div class="service-card">
        <span>互动服务</span>
        <el-tag :type="siteSettings.services.interactionServiceEnabled ? 'success' : 'info'">
          {{ siteSettings.services.interactionServiceEnabled ? '已开启' : '已关闭' }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  createDefaultAdminSiteSettings,
  loadAdminSiteSettings,
  normalizeAdminSiteSettings,
  saveAdminSiteSettings,
} from '@/utils/adminSiteSettings'

const siteSettings = reactive(createDefaultAdminSiteSettings())

const loadServices = () => {
  Object.assign(siteSettings, loadAdminSiteSettings())
}

const saveServices = () => {
  saveAdminSiteSettings(normalizeAdminSiteSettings(siteSettings))
  ElMessage.success('服务配置已保存')
}

onMounted(() => {
  loadServices()
})
</script>

<style scoped>
.service-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid rgba(148, 163, 184, 0.28);
  border-radius: 12px;
  background: rgba(248, 250, 252, 0.78);
}

.dark .service-card {
  border-color: rgba(71, 85, 105, 0.5);
  background: rgba(15, 23, 42, 0.68);
}
</style>