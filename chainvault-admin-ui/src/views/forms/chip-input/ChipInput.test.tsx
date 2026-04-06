import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import ChipInput from './ChipInput'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('ChipInput', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<ChipInput />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
