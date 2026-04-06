import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Alerts from './Alerts'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Alerts', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Alerts />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
