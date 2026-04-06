import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Breadcrumbs from './Breadcrumbs'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Breadcrumbs', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Breadcrumbs />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
