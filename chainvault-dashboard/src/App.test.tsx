/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { render, screen } from '@testing-library/react'
import { describe, it, vi, expect } from 'vitest'
import { createTheme } from '@mui/material'
import App from './App'

// 1. Create a real (but basic) MUI theme for the mock
const mockTheme = createTheme({
  palette: {
    mode: 'light',
  },
})

vi.mock('./theme', () => ({
  // We must return a valid MUI theme object so CssBaseline doesn't crash
  useMode: vi.fn(() => [mockTheme, { toggleColorMode: vi.fn() }]),
  ColorModeContext: {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    Provider: ({ children }: any) => <div data-testid="color-provider">{children}</div>,
  },
}))

// 2. Mock scenes
vi.mock('./scenes', () => ({
  Dashboard: () => <div data-testid="mock-dashboard">Dashboard</div>,
  Navbar: () => <nav data-testid="mock-navbar">Navbar</nav>,
}))

describe('App Component', () => {
  it('renders the Navbar and Dashboard within the providers', () => {
    render(<App />)

    expect(screen.getByTestId('mock-navbar')).toBeInTheDocument()
    expect(screen.getByTestId('mock-dashboard')).toBeInTheDocument()
  })

  it('applies the flex container styles for layout', () => {
    const { container } = render(<App />)

    // Select the first Box (the wrapper)
    const flexBox = container.querySelector('.MuiBox-root')
    expect(flexBox).toHaveStyle({
      display: 'flex',
      flexDirection: 'column',
      minHeight: '100vh',
    })
  })

  it('renders the MUI container', () => {
    render(<App />)
    // Check for the presence of the container class
    const muiContainer = document.querySelector('.MuiContainer-root')
    expect(muiContainer).toBeInTheDocument()
  })
})
