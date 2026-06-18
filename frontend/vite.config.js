import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    host: '127.0.0.1',
    port: 18113,
    strictPort: true,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:19113',
        changeOrigin: true
      }
    }
  },
  preview: {
    host: '127.0.0.1',
    port: 18113,
    strictPort: true
  }
})
