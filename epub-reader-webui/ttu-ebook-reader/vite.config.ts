import {defineConfig} from 'vite'
import {svelte} from '@sveltejs/vite-plugin-svelte'
import {viteSingleFile} from 'vite-plugin-singlefile';

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    svelte(),
    viteSingleFile({useRecommendedBuildConfig: true}),
  ],
  resolve: {
    alias: {
      $lib: "/src/lib",
    },
  },
  css: {
    preprocessorOptions: {
      scss: {
        api: 'modern-compiler' // or "modern"
      }
    }
  },
  build: {
    rollupOptions: {
      input: {
        app: './ttsu.html',
      },
    },
  },
})
