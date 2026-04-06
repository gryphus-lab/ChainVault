import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Flags from './Flags'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Flags', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Flags />)
    await waitFor(
      () => {
        expect(container.firstChild).toBeTruthy()
      },
      { timeout: 60000 },
    )
  })
})
