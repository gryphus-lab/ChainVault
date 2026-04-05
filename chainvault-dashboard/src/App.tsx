/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { useMemo, useState, ReactElement } from 'react'
import { Box, CssBaseline, ThemeProvider, Container, Theme } from '@mui/material'
import { ColorModeContext, useMode } from './theme'
import { Dashboard, Navbar } from './scenes'
import { ToggledContext } from './context/ToggledContext'

function App(): ReactElement {
  // Ensure useMode returns [Theme, { toggleColorMode: () => void }]
  const [theme, colorMode] = useMode() as [Theme, { toggleColorMode: () => void }]

  const [toggled, setToggled] = useState<boolean>(false)

  // Memoize the context value with explicit types
  const values = useMemo(
    () => ({ toggled, setToggled }),
    [toggled], // setToggled is stable, but adding [toggled, setToggled] is also fine
  )

  return (
    <ColorModeContext.Provider value={colorMode}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <ToggledContext.Provider value={values}>
          <Box
            sx={{
              display: 'flex',
              flexDirection: 'column',
              minHeight: '100vh',
            }}
          >
            <Navbar />
            <Dashboard />
            <Container
              maxWidth="xl"
              sx={{
                flex: 1,
                py: 2,
                display: 'flex',
                flexDirection: 'column',
              }}
            ></Container>
          </Box>
        </ToggledContext.Provider>
      </ThemeProvider>
    </ColorModeContext.Provider>
  )
}

export default App
