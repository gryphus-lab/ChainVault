import { Box } from "@mui/material";
import { StatBox } from "../../components";
import PowerIcon from "@mui/icons-material/Power";
import LocalGasStationIcon from "@mui/icons-material/LocalGasStation";
import ElectricalServicesIcon from "@mui/icons-material/ElectricalServices";
import WarningAmberIcon from "@mui/icons-material/WarningAmber";

const FeetOverviewDataBox = ({
  colors,
  selectedData,
  openMetricTrendModal,
}) => {
  return (
    <>
      <Box
        gridColumn="span 3"
        bgcolor={colors.primary[400]}
        display="flex"
        alignItems="center"
        justifyContent="center"
        sx={{ cursor: "pointer" }}
        onClick={() =>
          openMetricTrendModal(
            "Total Real Power",
            parseFloat(selectedData?.totalRealPower),
            "line",
          )
        }
      >
        <StatBox
          title={selectedData?.totalRealPower}
          subtitle="Total Real Power (kW)"
          //   progress="0.75"
          //   increase="+14%"
          icon={
            <PowerIcon
              sx={{ color: colors.greenAccent[600], fontSize: "26px" }}
            />
          }
        />
      </Box>
      <Box
        gridColumn="span 3"
        backgroundColor={colors.primary[400]}
        display="flex"
        alignItems="center"
        justifyContent="center"
        sx={{ cursor: "pointer" }}
        onClick={() =>
          openMetricTrendModal(
            "Total Fuel Consumption",
            parseFloat(selectedData?.totalFuelConsumption),
            "line",
          )
        }
      >
        <StatBox
          title={selectedData?.totalFuelConsumption}
          subtitle="Total Fuel Consumption (GPH)"
          //   progress="0.50"
          //   increase="+21%"
          icon={
            <LocalGasStationIcon
              sx={{ color: colors.greenAccent[600], fontSize: "26px" }}
            />
          }
        />
      </Box>
      <Box
        gridColumn="span 3"
        backgroundColor={colors.primary[400]}
        display="flex"
        alignItems="center"
        justifyContent="center"
        sx={{ cursor: "pointer" }}
        onClick={() =>
          openMetricTrendModal(
            "Active Generators",
            parseInt(selectedData?.activeGenerators.split("/")[0]),
            "bar",
          )
        }
      >
        <StatBox
          title={selectedData?.activeGenerators}
          subtitle="Active Generators"
          //   progress="0.30"
          //   increase="+5%"
          icon={
            <ElectricalServicesIcon
              sx={{ color: colors.greenAccent[600], fontSize: "26px" }}
            />
          }
        />
      </Box>
      <Box
        gridColumn="span 3"
        backgroundColor={colors.primary[400]}
        display="flex"
        alignItems="center"
        justifyContent="center"
        sx={{ cursor: "pointer" }}
        onClick={() =>
          openMetricTrendModal(
            "System Alerts",
            parseInt(selectedData?.systemAlerts),
            "bar",
          )
        }
      >
        <StatBox
          title={selectedData?.systemAlerts}
          subtitle="System Alerts"
          //   progress="0.80"
          //   increase="+43%"
          icon={
            <WarningAmberIcon
              sx={{ color: colors.redAccent[600], fontSize: "26px" }}
            />
          }
        />
      </Box>
    </>
  );
};

export default FeetOverviewDataBox;
