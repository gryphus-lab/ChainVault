import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import App from './App'
import { renderAppTree, resetStore } from './test/test-utils'

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
