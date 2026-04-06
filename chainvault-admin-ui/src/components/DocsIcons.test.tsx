import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import DocsIcons from './DocsIcons'
import { renderWithProviders, resetStore } from '../test/test-utils'

describe('DocsIcons', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<DocsIcons />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
