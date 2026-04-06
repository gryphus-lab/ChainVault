import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import AppHeaderDropdown from './AppHeaderDropdown'
import { renderWithProviders, resetStore } from '../../test/test-utils'

describe('AppHeaderDropdown', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<AppHeaderDropdown />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
