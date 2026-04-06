import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Buttons from './Buttons'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Buttons', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Buttons />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
