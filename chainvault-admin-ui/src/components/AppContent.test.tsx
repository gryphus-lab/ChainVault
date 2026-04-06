import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import AppContent from './AppContent'
import { renderWithProviders, resetStore } from '../test/test-utils'

describe('AppContent', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<AppContent />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
