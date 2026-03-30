import { Box, useTheme, Typography } from "@mui/material";
import { DataGrid } from "@mui/x-data-grid";
import { allGeneratorsData } from "../../data/allGeneratorsData";
import { tokens } from "../../theme";

const GeneratorStatus = () => {
  const theme = useTheme();
  const colors = tokens(theme.palette.mode);

  const columns = [
    { field: "id", headerName: "Generator ID", flex: 0.5 },
    { field: "subId", headerName: "Sub ID" },
    {
      field: "status",
      headerName: "Status",
      flex: 1,
      cellClassName: "name-column--cell",
    },
    {
      field: "location",
      headerName: "Location",
      flex: 1,
    },
    {
      field: "engineLoad",
      headerName: "Engine Load (%)",
      flex: 1,
    },
    {
      field: "fuelLevel",
      headerName: "Fuel Level",
      flex: 1,
    },
    {
      field: "engineHours",
      headerName: "Engine Hours",
      flex: 1,
    },
    {
      field: "engineSpeed",
      headerName: "Engine Speed (RPM)",
      flex: 1,
    },
    {
      field: "oilPressure",
      headerName: "Oil Pressure (PSI)",
      flex: 1,
    },
    {
      field: "batteryVoltage",
      headerName: "Battery Voltage (V)",
      flex: 1,
    },
    {
      field: "fuelConsRate",
      headerName: "Fuel Cons. Rate (GPH)",
      flex: 1,
    },
    {
      field: "apparentPower",
      headerName: "Apparent Power (kVA)",
      flex: 1,
    },
    {
      field: "kwHoursExport",
      headerName: "kW Hours Export",
      flex: 1,
    },
  ];
  return (
    <>
      <Box
        height="75vh"
        maxWidth="100%"
        gridColumn="span 12"
        mb="10px"
        sx={{
          "& .MuiDataGrid-root": {
            border: "none",
          },
          "& .MuiDataGrid-cell": {
            border: "none",
          },
          "& .name-column--cell": {
            color: colors.greenAccent[300],
          },
          "& .MuiDataGrid-columnHeaders": {
            backgroundColor: colors.blueAccent[700],
            borderBottom: "none",
          },
          "& .MuiDataGrid-virtualScroller": {
            backgroundColor: colors.primary[400],
          },
          "& .MuiDataGrid-footerContainer": {
            borderTop: "none",
            backgroundColor: colors.blueAccent[700],
          },
          "& .MuiCheckbox-root": {
            color: `${colors.greenAccent[200]} !important`,
          },
          "& .MuiDataGrid-iconSeparator": {
            color: colors.primary[100],
          },
          "& .MuiDataGrid-toolbarContainer .MuiButton-text": {
            color: `${colors.gray[100]} !important`,
          },
          "& .css-1uh4g4p .MuiDataGrid-columnHeaderTitle": {
            color: `${colors.primary[100]} !important`,
          },
        }}
      >
        <Box mb="10px">
          <Typography variant="h2" fontWeight="600" color={colors.gray[100]}>
            Generator Status
          </Typography>
        </Box>
        <DataGrid
          rows={allGeneratorsData}
          columns={columns}
          //   components={{ Toolbar: GridToolbar }}
          initialState={{
            pagination: {
              paginationModel: {
                pageSize: 10,
              },
            },
          }}
          //   checkboxSelection
        />
      </Box>
    </>
  );
};

export default GeneratorStatus;
