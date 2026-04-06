import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import DocsComponents from './DocsComponents'
import { renderWithProviders, resetStore } from '../test/test-utils'

describe('DocsComponents', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<DocsComponents href={''} />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
