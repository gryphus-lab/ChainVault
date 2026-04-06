import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import CoreUIIcons from './CoreUIIcons'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('CoreUIIcons', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<CoreUIIcons />)
    await waitFor(
      () => {
        expect(container.firstChild).toBeTruthy()
      },
      { timeout: 60000 },
    )
  })
})
