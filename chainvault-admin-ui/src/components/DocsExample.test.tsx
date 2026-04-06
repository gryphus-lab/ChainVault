import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import DocsExample from './DocsExample'
import { renderWithProviders, resetStore } from '../test/test-utils'

describe('DocsExample', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<DocsExample children={undefined} href={''} />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
