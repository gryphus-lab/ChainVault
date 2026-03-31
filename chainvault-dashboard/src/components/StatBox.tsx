/*
 * Copyright (c) 2026. Gryphus Lab
 */
import React from "react";
import { Box, Typography, useTheme } from "@mui/material";
import { tokens } from "@/theme";
import ProgressCircle from "./ProgressCircle";

// 1. Defined an Interface for Props
interface StatBoxProps {
  title: string;
  subtitle: string;
  icon?: React.ReactNode;
  increase?: string;
}

const StatBox: React.FC<StatBoxProps> = ({
  title,
  subtitle,
  icon,
  increase,
}) => {
  const theme = useTheme();
  const colors = tokens(theme.palette.mode);

  return (
    <Box width="100%" m="0 30px">
      <Box display="flex" justifyContent="space-between">
        <Box>
          {/* Render icon with a margin if it exists */}
          {icon && <Box sx={{ mb: "5px" }}>{icon}</Box>}
          <Typography
            variant="h4" // Changed from h2 to h4 for better visual hierarchy
            fontWeight="bold"
            sx={{ color: colors.gray[100] }}
          >
            {title}
          </Typography>
        </Box>
        <Box>
          <ProgressCircle />
        </Box>
      </Box>

      <Box display="flex" justifyContent="space-between" mt="2px">
        <Typography
          variant="h5"
          sx={{
            color:
              subtitle === "System Alerts"
                ? colors.redAccent[500]
                : colors.greenAccent[500],
          }}
        >
          {subtitle}
        </Typography>
        <Typography
          variant="h5"
          fontStyle="italic"
          sx={{ color: colors.greenAccent[600] }}
        >
          {increase}
        </Typography>
      </Box>
    </Box>
  );
};

export default StatBox;
