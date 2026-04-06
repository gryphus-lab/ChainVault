import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import WidgetsBrand from './WidgetsBrand'
import { renderWithProviders, resetStore } from '../../test/test-utils'

describe('WidgetsBrand', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<WidgetsBrand />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
