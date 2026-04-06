import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import ButtonGroups from './ButtonGroups'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('ButtonGroups', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<ButtonGroups />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
