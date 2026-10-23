import { defineConfig } from 'vite';

export default defineConfig({
  server: { port: 5173, strictPort: true, proxy: { '/api': { target: process.env.API_URL || 'http://localhost:8080' } } },
  preview: { port: 4173, strictPort: true },
});
