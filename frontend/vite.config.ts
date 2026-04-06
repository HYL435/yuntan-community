import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite' // 导入tailwindcss
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  // 读取当前模式对应的环境变量（development / production）
  // loadEnv 第三参数为空字符串表示读取所有前缀的变量（包括 VITE_ 开头）
  const env = loadEnv(mode, process.cwd(), '')

  // 代理目标：开发模式指向 yuntan-blog.top，生产模式指向 localhost
  const apiTarget = env.VITE_API_TARGET || 'http://localhost'

  return {
    plugins: [
      vue(),
      tailwindcss() // 使用tailwindcss
    ],
    server: {
      hmr: {
        overlay: false
      },
      proxy: {
        '/front': {
          target: apiTarget,
          changeOrigin: true
        },
        // 使用 /api 前缀代理后端，避免与前端 /admin 路由冲突
        '/api': {
          target: apiTarget,
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, '')
        }
      }
    },
    resolve: {
      alias: {
        '@': resolve(__dirname, './src')
      }
    }
  }
})