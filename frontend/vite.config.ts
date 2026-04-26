import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'
import { resolve } from 'path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const apiTarget = env.VITE_API_TARGET || 'http://localhost'

  return {
    plugins: [
      vue(),
      tailwindcss(),
    ],
    server: {
      hmr: {
        overlay: false,
      },
      proxy: {
        '/front': {
          target: apiTarget,
          changeOrigin: true,
        },
        '/api': {
          target: apiTarget,
          changeOrigin: true,
          timeout: 0,
          proxyTimeout: 0,
          rewrite: (path) => path.replace(/^\/api/, ''),
          // Keep SSE responses uncompressed and unbuffered through the dev proxy.
          configure: (proxy) => {
            proxy.on('proxyReq', (proxyReq) => {
              proxyReq.setHeader('Accept-Encoding', 'identity')
              proxyReq.setHeader('Cache-Control', 'no-cache')
              proxyReq.setHeader('Connection', 'keep-alive')
            })
            proxy.on('proxyRes', (proxyRes) => {
              proxyRes.headers['cache-control'] = 'no-cache, no-transform'
              proxyRes.headers['x-accel-buffering'] = 'no'
            })
          },
        },
      },
    },
    resolve: {
      alias: {
        '@': resolve(__dirname, './src'),
      },
    },
    build: {
      cssCodeSplit: true,
      rollupOptions: {
        output: {
          manualChunks(id) {
            if (!id.includes('node_modules')) return
            if (id.includes('element-plus') || id.includes('@element-plus')) return 'vendor-element'
            if (/node_modules\/(vue|@vue|pinia|vue-router)\//.test(id)) return 'vendor-vue'
            if (id.includes('md-editor-v3') || id.includes('markdown-it') || id.includes('highlight.js')) return 'vendor-markdown'
            if (id.includes('echarts')) return 'vendor-echarts'
            return undefined
          },
        },
      },
    },
  }
})
