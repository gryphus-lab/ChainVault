import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Placeholders from './Placeholders'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Placeholders', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Placeholders />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
