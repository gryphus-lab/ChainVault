import { createContext, useMemo, useState } from "react";
import { Box, CssBaseline, ThemeProvider, Container } from "@mui/material";
import { ColorModeContext, useMode } from "./theme";
import { Navbar } from "./scenes";
import { Outlet } from "react-router-dom";

export const ToggledContext = createContext(null);

function App() {
  const [theme, colorMode] = useMode();
  const [toggled, setToggled] = useState(false);
  const values = useMemo(() => ({ toggled, setToggled }), []);

  return (
    <ColorModeContext.Provider value={colorMode}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <ToggledContext.Provider value={values}>
          <Box sx={{ display: "flex", height: "100vh", maxWidth: "100%" }}>
            <Box
              sx={{
                flexGrow: 1,
                display: "flex",
                flexDirection: "column",
                height: "100%",
                maxWidth: "100%",
              }}
            >
              <Navbar />
              <Box sx={{ overflowY: "auto", flex: 1, maxWidth: "100%" }}>
                <Outlet />
              </Box>
            </Box>
          </Box>
          <Box
            sx={{
              display: "flex",
              flexDirection: "column",
              minHeight: "100vh",
            }}
          >
            <Navbar />
            <Container
              maxWidth="xl"
              sx={{
                flex: 1,
                py: 2, 
                display: "flex",
                flexDirection: "column",
              }}
            >
              <Box sx={{ flex: 1 }}>
                <Outlet />
              </Box>
            </Container>
          </Box>
        </ToggledContext.Provider>
      </ThemeProvider>
    </ColorModeContext.Provider>
  );
}

export default App;
