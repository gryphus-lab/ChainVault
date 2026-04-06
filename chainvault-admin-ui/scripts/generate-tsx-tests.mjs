/**
 * One-off generator: co-located *.test.tsx smoke tests for each *.tsx module (excluding existing tests).
 * Run: node scripts/generate-tsx-tests.mjs
 */
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const srcRoot = path.join(__dirname, '..', 'src')

const SKIP_BASE = new Set(['index.tsx'])

const HEAVY = new Set(['Brands.tsx', 'Flags.tsx', 'CoreUIIcons.tsx'])

function* walkTsx(dir) {
  const entries = fs.readdirSync(dir, { withFileTypes: true })
  for (const e of entries) {
    const full = path.join(dir, e.name)
    if (e.isDirectory()) {
      const relDir = path.relative(srcRoot, full).replace(/\\/g, '/')
      if (relDir === 'test') continue
      yield* walkTsx(full)
    } else if (e.name.endsWith('.tsx')) {
      yield full
    }
  }
}

function defaultExportTest(componentName, relImport, timeoutMs) {
  const waitForBlock = timeoutMs
    ? `await waitFor(
      () => {
        expect(container.firstChild).toBeTruthy()
      },
      { timeout: ${timeoutMs} },
    )`
    : `await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })`
  return `import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import ${componentName} from '${relImport}'
import { renderWithProviders, resetStore } from 'src/test/test-utils'

describe('${componentName}', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<${componentName} />)
    ${waitForBlock}
  })
})
`
}

function appTest() {
  return `import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import App from './App'
import { renderAppTree, resetStore } from 'src/test/test-utils'

describe('App', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders a route shell', async () => {
    const { container } = renderAppTree(<App />)
    await waitFor(
      () => {
        expect(container.firstChild).toBeTruthy()
      },
      { timeout: 15_000 },
    )
  })
})
`
}

function navTest() {
  return `import { describe, expect, it } from 'vitest'
import nav from './_nav'

describe('_nav', () => {
  it('exports a non-empty navigation config', () => {
    expect(Array.isArray(nav)).toBe(true)
    expect(nav.length).toBeGreaterThan(0)
  })
})
`
}

function namedAssetTest(name) {
  return `import { describe, expect, it } from 'vitest'
import { ${name} } from './${name}'

describe('${name}', () => {
  it('exports icon path data', () => {
    expect(Array.isArray(${name})).toBe(true)
    expect(${name}.length).toBeGreaterThan(0)
  })
})
`
}

let created = 0
for (const file of walkTsx(srcRoot)) {
  const rel = path.relative(srcRoot, file).replace(/\\/g, '/')
  const base = path.basename(file)

  if (base.endsWith('.test.tsx') || base.endsWith('.spec.tsx')) continue
  if (SKIP_BASE.has(base) && rel === 'index.tsx') continue

  const nameWithoutExt = path.basename(file, '.tsx')
  const dir = path.dirname(file)
  const testFile = path.join(dir, `${nameWithoutExt}.test.tsx`)
  if (fs.existsSync(testFile)) continue

  let body
  if (rel === 'App.tsx') {
    body = appTest()
  } else if (rel === '_nav.tsx') {
    body = navTest()
  } else if (rel === 'assets/brand/logo.tsx') {
    body = namedAssetTest('logo')
  } else if (rel === 'assets/brand/sygnet.tsx') {
    body = namedAssetTest('sygnet')
  } else {
    const relImport = `./${nameWithoutExt}`
    const timeout = HEAVY.has(base) ? 60_000 : undefined
    body = defaultExportTest(nameWithoutExt, relImport, timeout)
  }

  fs.writeFileSync(testFile, body, 'utf8')
  created += 1
}

console.log(`Wrote ${created} new *.test.tsx files under src/`)
