
import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
    // Load .env files AND real OS environment variables
    const env = { ...process.env, ...loadEnv(mode, process.cwd(), '') };
    const apiTarget = env.VITE_API_TARGET || 'http://localhost:8080';

    console.log(`[vite.config] Proxying /api to: ${apiTarget}`);

    return {
        plugins: [react()],
        server: {
            port: 5173,
            watch: {
                usePolling: true,
            },
            proxy: {
                '/api': {
                    target: apiTarget,
                    changeOrigin: true,
                    secure: false,
                }
            },
            allowedHosts: [
                "frontend"
            ]
        }
    };
});
