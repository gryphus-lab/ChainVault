import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Collapses from './Collapses'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Collapses', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Collapses />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
