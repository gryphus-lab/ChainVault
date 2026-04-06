import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Progress from './Progress'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Progress', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Progress />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
