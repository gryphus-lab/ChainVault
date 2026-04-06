import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Tooltips from './Tooltips'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Tooltips', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Tooltips />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
