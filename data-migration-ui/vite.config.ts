import { defineConfig } from "vite"
import react from "@vitejs/plugin-react"

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [react()],
    build: {
        rollupOptions: {
            input: {
                index: "src/Index.tsx"
            },
            output: {
                dir: "dist",
                entryFileNames: "data-migration-ui-bundle.js",
                inlineDynamicImports: false
            }
        },
        minify: false
    }
})
