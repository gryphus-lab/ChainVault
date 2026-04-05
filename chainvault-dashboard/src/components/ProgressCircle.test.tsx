/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import ProgressCircle from './ProgressCircle'

vi.mock('@mui/material', async () => {
  const actual = await vi.importActual('@mui/material')
  return {
    ...actual,
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    Box: ({ sx, ...props }: any) => (
      // We pass the sx object to a data-attribute so we can read it as a string
      <div data-testid="progress-box" data-sx={JSON.stringify(sx)} {...props} />
    ),
  }
})

vi.mock('@/theme', () => ({
  tokens: () => ({
    primary: { 400: '#000' },
    blueAccent: { 500: '#00f' },
    greenAccent: { 500: '#0f0' },
  }),
}))

describe('ProgressCircle Component', () => {
  const getSx = (element: HTMLElement) => JSON.parse(element.getAttribute('data-sx') || '{}')

  it('renders with default size and progress (0.75)', () => {
    render(<ProgressCircle />)
    const box = screen.getByTestId('progress-box')
    const sx = getSx(box)

    expect(sx.width).toBe('40px')
    // 0.75 * 360 = 270
    expect(sx.background).toContain('270deg')
  })

  it('calculates the correct angle for 50% progress', () => {
    render(<ProgressCircle progress={0.5} />)
    const box = screen.getByTestId('progress-box')
    const sx = getSx(box)

    // 0.5 * 360 = 180
    expect(sx.background).toContain('180deg')
  })

  it('handles 0% and 100% progress boundaries', () => {
    const { rerender } = render(<ProgressCircle progress={0} />)
    let sx = getSx(screen.getByTestId('progress-box'))
    expect(sx.background).toContain('0deg')

    rerender(<ProgressCircle progress={1} />)
    sx = getSx(screen.getByTestId('progress-box'))
    expect(sx.background).toContain('360deg')
  })
})
