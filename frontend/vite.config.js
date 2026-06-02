var _a;
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
// The dev server proxies /api to the Spring Boot backend so the frontend can
// call relative URLs and avoid CORS during local development.
export default defineConfig({
    plugins: [react()],
    server: {
        port: 5173,
        proxy: {
            "/api": {
                target: (_a = process.env.VITE_API_TARGET) !== null && _a !== void 0 ? _a : "http://localhost:8080",
                changeOrigin: true,
            },
        },
    },
});
