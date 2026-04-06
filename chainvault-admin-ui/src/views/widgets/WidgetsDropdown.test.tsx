import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import WidgetsDropdown from './WidgetsDropdown'
import { renderWithProviders, resetStore } from '../../test/test-utils'

describe('WidgetsDropdown', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<WidgetsDropdown />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
