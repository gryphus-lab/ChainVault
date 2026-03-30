import { useState } from "react";
import { Box } from "@mui/material";
import Chart from "react-apexcharts";
import AvgEngineLoadsChart from "./avgEngineLoadsChart";
import FleetFuelLevelChart from "./fleetFuelLevelChart";
import FeetOverviewDataBox from "./feetOverviewDataBox";
import { availableMetrics } from "../../data/availableMetrics";
import MetricTrendModal from "./metricTrendModal";

const AvgEngineLoadsFleetFuelLevel = ({
  colors,
  isXlDevices,
  selectedData,
}) => {
  const [modalMetricTitle, setModalMetricTitle] = useState();
  const [modalChartType, setModalChartType] = useState("line");
  const [modalMetricData, setModalMetricData] = useState([]);
  const [isMetricTrendModalOpen, setIsMetricTrendModalOpen] = useState(false);
  const openMetricTrendModal = (metricType, metricValue, chartType) => {
    setModalMetricTitle(`Historical Trend (${metricType})`);
    setModalChartType(chartType);

    const metricConfig = availableMetrics.find((m) => m?.label === metricType);
    const dataPoints = [];
    const labels = [];

    for (let i = 0; i < 24; i++) {
      labels.push(`${i}:00`);
      const fluctuation =
        (Math.random() - 0.5) * (metricConfig?.fluctuation || 20);
      dataPoints.push(
        Math.max(
          0,
          Math.min(metricConfig?.max || 100, metricValue + fluctuation),
        ),
      );
    }
    setModalMetricData(
      dataPoints.map((value, index) => ({ name: labels[index], value: value })),
    );
    setIsMetricTrendModalOpen(true);
  };

  const ApexGaugeChart = ({ title, value }) => {
    const getApexColor = (val) => {
      if (val <= 80) return "#4ade80";
      if (val <= 90) return "#facc15";
      return "#f87171";
    };

    const fillColor = getApexColor(value);

    const options = {
      chart: { type: "radialBar", sparkline: { enabled: true } },
      plotOptions: {
        radialBar: {
          startAngle: -135,
          endAngle: 135,
          hollow: { size: "60%" },
          track: {
            background: "#e0e0e0",
            strokeWidth: "100%",
          },
          dataLabels: {
            name: {
              fontSize: "14px",
              offsetY: 30,
            },
            value: {
              fontSize: "24px",
              offsetY: -10,
              formatter: (val) => val + "%",
              color: fillColor,
            },
          },
        },
      },
      labels: [title],
      fill: {
        colors: [fillColor],
      },
    };

    return (
      <Chart
        options={options}
        series={[value]}
        type="radialBar"
        height={400}
        width={350}
      />
    );
  };


  return (
    <>
      <Box
        gridColumn={isXlDevices ? "span 4" : "span 4"}
        gridRow="span 2"
        backgroundColor={colors.primary[400]}
        p="30px"
        sx={{ cursor: "pointer" }}
        onClick={() =>
          openMetricTrendModal(
            "Average Engine Load",
            selectedData?.avgEngineLoad,
            "line",
          )
        }
      >
        <Box
          display="flex"
          alignItems="center"
          justifyContent="center"
          height="250px"
          mt="-20px"
        >
          <AvgEngineLoadsChart
            ApexGaugeChart={ApexGaugeChart}
            selectedData={selectedData}
          />
        </Box>
      </Box>

      <Box
        gridColumn={isXlDevices ? "span 4" : "span 4"}
        gridRow="span 2"
        backgroundColor={colors.primary[400]}
        p="30px"
        sx={{ cursor: "pointer" }}
        onClick={() =>
          openMetricTrendModal(
            "Fleet Fuel Level",
            selectedData?.fleetFuelLevel,
            "line",
          )
        }
      >
        <Box
          display="flex"
          alignItems="center"
          justifyContent="center"
          height="250px"
          mt="-20px"
        >
          <FleetFuelLevelChart
            ApexGaugeChart={ApexGaugeChart}
            selectedData={selectedData}
          />
        </Box>
      </Box>
      <FeetOverviewDataBox
        colors={colors}
        selectedData={selectedData}
        openMetricTrendModal={openMetricTrendModal}
      />
      <MetricTrendModal
        modalMetricTitle={modalMetricTitle}
        modalChartType={modalChartType}
        modalMetricData={modalMetricData}
        isOpen={isMetricTrendModalOpen}
        onClose={() => setIsMetricTrendModalOpen(false)}
        colors={colors}
      />
    </>
  );
};

export default AvgEngineLoadsFleetFuelLevel;
