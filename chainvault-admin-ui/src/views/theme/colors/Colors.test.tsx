/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import Colors from './Colors'

// --- mocks ---

// Mock CoreUI components (lightweight wrappers)
vi.mock('@coreui/react', () => ({
  CRow: ({ children }: any) => <div>{children}</div>,
  CCol: ({ children }: any) => <div>{children}</div>,
  CCard: ({ children }: any) => <div>{children}</div>,
  CCardHeader: ({ children }: any) => <div>{children}</div>,
  CCardBody: ({ children }: any) => <div>{children}</div>,
}))

// Mock DocsLink
vi.mock('../../../components', () => ({
  DocsLink: () => <a href="#">Docs</a>,
}))

// Mock rgbToHex
vi.mock('@coreui/utils', () => ({
  rgbToHex: vi.fn(() => '#ffffff'),
}))

describe('Colors', () => {
  beforeEach(() => {
    vi.clearAllMocks()

    // Mock getComputedStyle globally
    vi.spyOn(globalThis, 'getComputedStyle').mockReturnValue({
      getPropertyValue: () => 'rgb(255, 255, 255)',
    } as any)
  })

  it('renders header and docs link', () => {
    render(<Colors />)

    expect(screen.getByText('Theme colors')).toBeInTheDocument()
    expect(screen.getByText('Docs')).toBeInTheDocument()
  })

  it('renders all theme color labels', () => {
    render(<Colors />)

    expect(screen.getByText('Brand Primary Color')).toBeInTheDocument()
    expect(screen.getByText('Brand Secondary Color')).toBeInTheDocument()
    expect(screen.getByText('Brand Success Color')).toBeInTheDocument()
    expect(screen.getByText('Brand Danger Color')).toBeInTheDocument()
    expect(screen.getByText('Brand Warning Color')).toBeInTheDocument()
    expect(screen.getByText('Brand Info Color')).toBeInTheDocument()
    expect(screen.getByText('Brand Light Color')).toBeInTheDocument()
    expect(screen.getByText('Brand Dark Color')).toBeInTheDocument()
  })

  it('renders multiple ThemeView tables', () => {
    render(<Colors />)

    // each ThemeColor renders a table
    const tables = screen.getAllByRole('table')
    expect(tables.length).toBe(8)
  })

  it('displays HEX and RGB values', async () => {
    render(<Colors />)

    // HEX from mocked rgbToHex
    expect(await screen.findAllByText('#ffffff')).toHaveLength(8)

    // RGB from mocked getComputedStyle
    expect(await screen.findAllByText('rgb(255, 255, 255)')).toHaveLength(8)
  })
})
