import {
  Box,
  useMediaQuery,
  useTheme,
  Typography,
  Select,
  MenuItem,
} from "@mui/material";
import { Header } from "../../components";
import AvgEngineLoadsFleetFuelLevel from "./avgEngineLoadsFleetFuelLevel";
import GeneratorStatus from "./generatorStatus";
import { fleetData } from "../../data/fleetData";
import { useState } from "react";
import { tokens } from "../../theme";

function Dashboard() {
  const theme = useTheme();
  const colors = tokens(theme.palette.mode);
  const isXlDevices = useMediaQuery("(min-width: 1260px)");
  const isMdDevices = useMediaQuery("(min-width: 724px)");
  const isXsDevices = useMediaQuery("(max-width: 436px)");
  const [selectedRegion, setSelectedRegion] = useState(
    Object.keys(fleetData)[0],
  );
  const selectedData = fleetData[selectedRegion];

  return (
    <Box m="20px">
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Header title="Fleet Overview" />
        {!isXsDevices && (
          <Box mt="20px" display="flex" alignItems="center" gap={1}>
            <Typography variant="body1" fontWeight="bold">
              Select Region:
            </Typography>
            <Select
              value={selectedRegion}
              onChange={(e) => setSelectedRegion(e.target.value)}
              size="small"
              sx={{
                minWidth: 150,
                fontWeight: "bold",
                boxShadow: 1,
                borderRadius: "6px",
              }}
            >
              {Object.keys(fleetData).map((region) => (
                <MenuItem key={region} value={region}>
                  {region}
                </MenuItem>
              ))}
            </Select>
          </Box>
        )}
      </Box>

      <Box
        display="grid"
        gridTemplateColumns={
          isXlDevices
            ? "repeat(12, 1fr)"
            : isMdDevices
              ? "repeat(6, 1fr)"
              : "repeat(3, 1fr)"
        }
        gridAutoRows="140px"
        gap="20px"
      >
        <AvgEngineLoadsFleetFuelLevel
          theme={theme}
          colors={colors}
          isXlDevice={isXlDevices}
          selectedData={selectedData}
        />

        <GeneratorStatus selectedData={selectedData} />
      </Box>
    </Box>
  );
}

export default Dashboard;
