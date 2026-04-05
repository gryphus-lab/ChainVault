/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { ColorModeContext } from '@/theme'
import { ToggledContext } from '@/context/ToggledContext'
import { useMediaQuery } from '@mui/material'
import Navbar from './index'

// 1. Mock the useMediaQuery hook
vi.mock('@mui/material', async () => {
  const actual = await vi.importActual('@mui/material')
  return { ...actual, useMediaQuery: vi.fn() }
})

// 2. Mock tokens
vi.mock('@/theme', async () => {
  const actual = await vi.importActual('@/theme')
  return {
    ...actual,
    tokens: () => ({ primary: { 400: '#123456' } }),
  }
})

describe('Navbar Component', () => {
  const mockToggleColorMode = vi.fn()
  const mockSetToggled = vi.fn()

  const renderNavbar = (toggled = false) => {
    return render(
      <ColorModeContext.Provider value={{ toggleColorMode: mockToggleColorMode }}>
        <ToggledContext.Provider value={{ toggled, setToggled: mockSetToggled }}>
          <Navbar />
        </ToggledContext.Provider>
      </ColorModeContext.Provider>,
    )
  }

  it('calls toggleColorMode when the theme icon is clicked', () => {
    renderNavbar()
    const themeButton = screen.getByTestId('DarkModeOutlinedIcon')
    fireEvent.click(themeButton)
    expect(mockToggleColorMode).toHaveBeenCalledTimes(1)
  })

  it('shows the Menu icon only on medium/mobile devices', () => {
    // Simulate mobile view
    vi.mocked(useMediaQuery).mockReturnValue(true)
    renderNavbar()

    const menuButton = screen.getByTestId('MenuOutlinedIcon').parentElement
    expect(menuButton).not.toHaveStyle({ display: 'none' })

    fireEvent.click(menuButton!)
    expect(mockSetToggled).toHaveBeenCalled()
  })

  it('hides the search bar on extra small devices', () => {
    // Mock isXsDevices = true
    vi.mocked(useMediaQuery).mockImplementation((query) => query === '(max-width:466px)')

    renderNavbar()
    const searchBox = screen.getByPlaceholderText(/search/i).closest('.MuiBox-root')
    expect(searchBox).toHaveStyle({ display: 'none' })
  })

  it('renders the search bar on desktop', () => {
    vi.mocked(useMediaQuery).mockReturnValue(false)
    renderNavbar()

    expect(screen.getByPlaceholderText(/search/i)).toBeInTheDocument()
  })
})
