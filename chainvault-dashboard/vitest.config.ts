import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'
import path from 'node:path'

export default defineConfig({
  plugins: [react()],

  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },

  test: {
    reporters: ['default', ['junit', { outputFile: './target/surefire-reports/junit.xml' }]],
    pool: 'forks',
    globals: true,
    environment: 'happy-dom',
    setupFiles: './src/test/setup.ts',
    include: ['src/**/*.test.{ts,tsx}', 'src/**/*.spec.{ts,tsx}'],
    exclude: ['node_modules', 'dist', '.idea', '.git', '.cache'],

    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html', 'lcov'],
      reportsDirectory: './coverage',
      exclude: [
        'node_modules/**',
        'dist/**',
        '**/*.d.ts',
        '**/*.test.{ts,tsx}',
        '**/*.spec.{ts,tsx}',
        'src/test/**',
        'src/**/*.test.*',
        'src/**/*.spec.*',
      ],
    },
  },
})
