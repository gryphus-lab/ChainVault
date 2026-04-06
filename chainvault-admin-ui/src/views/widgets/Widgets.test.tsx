import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Widgets from './Widgets'
import { renderWithProviders, resetStore } from '../../test/test-utils'

describe('Widgets', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Widgets />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
