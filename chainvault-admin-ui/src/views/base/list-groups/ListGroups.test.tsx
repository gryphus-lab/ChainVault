import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import ListGroups from './ListGroups'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('ListGroups', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<ListGroups />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
