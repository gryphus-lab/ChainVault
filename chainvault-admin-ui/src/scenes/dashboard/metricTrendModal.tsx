import { Modal, Box, Typography, IconButton } from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import Chart from "react-apexcharts";

const MetricTrendModal = ({
  modalMetricTitle,
  modalMetricData = [],
  modalChartType = "line",
  isOpen = true,
  onClose = () => {},
  colors,
}) => {
  const modalStyle = {
    position: "absolute",
    top: "50%",
    left: "50%",
    transform: "translate(-50%, -50%)",
    width: 700,
    maxHeight: "90vh",
    overflowY: "auto",
    backgroundColor: colors.primary[400],
    boxShadow: 24,
    borderRadius: "10px",
    p: 4,
  };
  const labels = modalMetricData?.map((d) => d?.name);
  const seriesData = modalMetricData?.map((d) =>
    d?.value !== null && d?.value !== undefined
      ? Number(d.value.toFixed(2))
      : 0,
  );

  const chartOptions = {
    chart: {
      type: modalChartType,
      height: 400,
      zoom: { enabled: true },
    },
    dataLabels: {
      enabled: false,
    },
    stroke: {
      curve: modalChartType === "line" ? "smooth" : "straight",
    },
    xaxis: {
      categories: labels,
      title: {
        text: "Time",
        style: { color: colors.gray[100] },
      },
      labels: {
        style: {
          colors: colors.gray[100],
        },
      },
    },
    yaxis: {
      title: {
        text: "Value",
        style: { color: colors.gray[100] },
      },
      labels: {
        style: {
          colors: colors.gray[100],
        },
      },
    },
    title: {
      //   text: modalMetricTitle,
      align: "center",
      style: {
        fontSize: "18px",
        fontWeight: "bold",
        color: colors.gray[100],
      },
    },
  };

  const chartSeries = [
    {
      //   name: modalMetricTitle,
      data: seriesData,
    },
  ];

  return (
    <Modal open={isOpen} onClose={onClose}>
      <Box sx={modalStyle}>
        <Box
          display="flex"
          justifyContent="space-between"
          alignItems="center"
          mb={2}
        >
          <Typography variant="h6">{modalMetricTitle}</Typography>
          <IconButton onClick={onClose} sx={{ ml: 2, color: "black" }}>
            <CloseIcon fontSize="medium" />
          </IconButton>
        </Box>
        <Chart
          options={chartOptions}
          series={chartSeries}
          type={modalChartType}
          height={400}
        />
      </Box>
    </Modal>
  );
};

export default MetricTrendModal;

// import React from "react";
// import { Modal, Box, Typography, IconButton } from "@mui/material";
// import CloseIcon from "@mui/icons-material/Close";
// import Plot from "react-plotly.js";

// const style = {
//   position: "absolute",
//   top: "50%",
//   left: "50%",
//   transform: "translate(-50%, -50%)",
//   width: "90%",
//   maxWidth: 800,
//   bgcolor: "background.paper",
//   borderRadius: 4,
//   boxShadow: 24,
//   p: 4,
// };

// const MetricTrendModal = ({
//   modalMetricTitle,
//   modalMetricData = [],
//   modalChartType = "line",
//   isOpen = true,
//   onClose = () => {},
// }) => {

//     const plotData = [
//     {
//       x: modalMetricData.map((d) => d.name),
//       y: modalMetricData.map((d) => d.value),
//       type: modalChartType === "bar" ? "bar" : "scatter",
//       mode: modalChartType === "line" ? "lines+markers" : undefined,
//       marker: { color: "#3f51b5" },
//     },
//   ];

//   return (
//     <Modal open={isOpen} onClose={onClose}>
//       <Box sx={style}>
//         <Box
//           display="flex"
//           justifyContent="space-between"
//           alignItems="center"
//           mb={2}
//         >
//           <Typography variant="h6">{modalMetricTitle}</Typography>
//           <IconButton onClick={onClose}>
//             <CloseIcon />
//           </IconButton>
//         </Box>

//         <Plot
//           data={plotData}
//           layout={{
//             title: "",
//             xaxis: { title: "Time" },
//             yaxis: { title: "Value" },
//             autosize: true,
//             margin: { t: 10, l: 40, r: 20, b: 40 },
//           }}
//           useResizeHandler
//           style={{ width: "100%", height: "100%" }}
//           config={{ responsive: true }}
//         />
//       </Box>
//     </Modal>
//   );
// };

// export default MetricTrendModal;
