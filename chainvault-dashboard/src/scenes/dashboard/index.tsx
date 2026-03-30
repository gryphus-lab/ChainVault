/*
 * Copyright (c) 2026. Gryphus Lab
 */
import {
  Box,
  useMediaQuery,
  useTheme
} from "@mui/material";
import { Header } from "@/components";
import { tokens } from "@/theme";

function Dashboard() {
  const theme = useTheme();
  tokens(theme.palette.mode);
  useMediaQuery("(max-width: 436px)");
  return (
    <Box m="20px">
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Header title="Fleet Overview" subtitle={undefined} />
      </Box>
    </Box>
  );
}

export default Dashboard;
