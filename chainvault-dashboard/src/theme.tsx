/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { createTheme, PaletteMode, Theme } from '@mui/material'
import { useMemo, useState, createContext } from 'react'

// Helper to reverse scales where light/dark are just mirrors
const reverseScale = (scale: Record<number, string>) =>
  Object.fromEntries(Object.entries(scale).reverse())

const SHARED_COLORS = {
  blueAccent: {
    100: '#e1e2fe',
    200: '#c3c6fd',
    300: '#a4a9fc',
    400: '#868dfb',
    500: '#6870fa',
    600: '#535ac8',
    700: '#3e4396',
    800: '#2a2d64',
    900: '#151632',
  },
  redAccentBase: {
    100: '#f8dcdb',
    200: '#f1b9b7',
    300: '#e99592',
    400: '#e2726e',
    500: '#db4f4a',
    600: '#af3f3b',
    700: '#832f2c',
    800: '#58201e',
    900: '#2c100f',
  },
  greenAccentBase: {
    100: '#dbf5ee',
    200: '#b7ebde',
    300: '#94e2cd',
    400: '#70d8bd',
    500: '#4cceac',
    600: '#3da58a',
    700: '#2e7c67',
    800: '#1e5245',
    900: '#0f2922',
  },
}

const DARK_COLORS = {
  gray: {
    100: '#e0e0e0',
    200: '#c2c2c2',
    300: '#a3a3a3',
    400: '#858585',
    500: '#666666',
    600: '#525252',
    700: '#3d3d3d',
    800: '#292929',
    900: '#141414',
  },
  primary: {
    100: '#d0d1d5',
    200: '#a1a4ab',
    300: '#727681',
    400: '#434957',
    500: '#141b2d',
    600: '#101624',
    700: '#0c101b',
    800: '#080b12',
    900: '#040509',
  },
  greenAccent: SHARED_COLORS.greenAccentBase,
  redAccent: SHARED_COLORS.redAccentBase,
  blueAccent: SHARED_COLORS.blueAccent,
  orangeAccent: {
    100: '#ffe8cc',
    200: '#ffd199',
    300: '#ffba66',
    400: '#ffa333',
    500: '#ff8c00',
    600: '#e67e00',
    700: '#cc7000',
    800: '#b36200',
    900: '#ffa500',
  },
}

const LIGHT_COLORS = {
  gray: reverseScale(DARK_COLORS.gray),
  primary: {
    100: '#040509',
    200: '#080b12',
    300: '#0c101b',
    400: '#fcfcfc',
    500: '#f2f0f0',
    600: '#434957',
    700: '#727681',
    800: '#a1a4ab',
    900: '#d0d1d5',
  },
  greenAccent: reverseScale(SHARED_COLORS.greenAccentBase),
  redAccent: reverseScale(SHARED_COLORS.redAccentBase),
  blueAccent: SHARED_COLORS.blueAccent,
  orangeAccent: {
    100: '#fff7ed',
    200: '#ffedd5',
    300: '#fed7aa',
    400: '#fdba74',
    500: '#fb923c',
    600: '#f97316',
    700: '#ea580c',
    800: '#c2410c',
    900: '#ffa500',
  },
}

export const tokens = (mode: PaletteMode) => (mode === 'dark' ? DARK_COLORS : LIGHT_COLORS)

const fontConfig = { fontFamily: ['Source Sans Pro', 'sans-serif'].join(',') }
const typographyLevels = ['h1', 'h2', 'h3', 'h4', 'h5', 'h6'] as const
const fontSizes: Record<string, number> = {
  h1: 40,
  h2: 32,
  h3: 24,
  h4: 20,
  h5: 16,
  h6: 14,
}

export const themeSettings = (mode: PaletteMode) => {
  const colors = tokens(mode)
  return {
    palette: {
      mode,
      primary: {
        main: mode === 'dark' ? colors.primary[500] : colors.primary[100],
      },
      secondary: { main: colors.greenAccent[500] },
      neutral: {
        dark: colors.gray[700],
        main: colors.gray[500],
        light: colors.gray[100],
      },
      background: { default: colors.primary[500] },
    },
    typography: {
      ...fontConfig,
      fontSize: 12,
      ...Object.fromEntries(
        typographyLevels.map((lvl) => [lvl, { ...fontConfig, fontSize: fontSizes[lvl] }]),
      ),
    },
  }
}

export const ColorModeContext = createContext({ toggleColorMode: () => {} })

export const useMode = (): [Theme, { toggleColorMode: () => void }] => {
  const [mode, setMode] = useState<PaletteMode>('dark')
  const colorMode = useMemo(
    () => ({
      toggleColorMode: () => setMode((prev) => (prev === 'light' ? 'dark' : 'light')),
    }),
    [],
  )

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const theme = useMemo(() => createTheme(themeSettings(mode) as any), [mode])
  return [theme, colorMode]
}
