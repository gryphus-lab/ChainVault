import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Validation from './Validation'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Validation', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Validation />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
