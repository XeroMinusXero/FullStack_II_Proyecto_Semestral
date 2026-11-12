import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'

// Todas las llamadas /api pasan por el API Gateway (8090). Sin CORS en desarrollo:
// el navegador solo habla con Vite (5173) y Vite reenvia al gateway.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': { target: 'http://localhost:8090', changeOrigin: true },
    },
  },
})
