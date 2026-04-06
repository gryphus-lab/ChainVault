import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import DocsLink from './DocsLink'

describe('DocsLink', () => {
  it('builds href from name and shows default docs label', () => {
    render(<DocsLink name="colors" />)
    const link = screen.getByRole('link', { name: /docs/i })
    expect(link).toHaveAttribute('href', 'https://coreui.io/react/docs/components/colors')
    expect(link).toHaveAttribute('target', '_blank')
  })

  it('uses explicit href when name is omitted', () => {
    render(<DocsLink href="https://example.com/docs" text="manual" />)
    const link = screen.getByRole('link', { name: 'manual' })
    expect(link).toHaveAttribute('href', 'https://example.com/docs')
  })
})
