import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Accordion from './Accordion'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Accordion', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Accordion />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
