import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Login from './Login'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Login', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Login />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
