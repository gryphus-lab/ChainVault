import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import InputGroup from './InputGroup'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('InputGroup', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<InputGroup />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
