/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { Box, useTheme } from "@mui/material";
import { DataGrid, GridColDef, GridRenderCellParams } from "@mui/x-data-grid";
import { tokens } from "@/theme";
import { safeFormat } from "@/lib/utils.ts";
import { Link } from "react-router-dom";

interface RowData {
  id: number;
  name: string;
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const MigrationDataGrid = (input: { data: any }) => {
  const gridData = Array.isArray(input.data) ? input.data : [];
  const theme = useTheme();
  const colors = tokens(theme.palette.mode);

  const formattedResults = gridData.map((item) => ({
    ...item,
    title: item.title || "Untitled",
    status: item.status,
    createdAt: safeFormat(item.createdAt),
    updatedAt: safeFormat(item.updatedAt),
  }));

  const columns: GridColDef[] = [
    { field: "id", headerName: "Migration ID" },
    {
      field: "status",
      headerName: "Status",
      cellClassName: "name-column--cell",
    },
    { field: "title", headerName: "Title" },
    {
      field: "createdAt",
      headerName: "Created",
    },
    {
      field: "updatedAt",
      headerName: "Updated",
    },
    {
      field: "action",
      headerName: "Actions",
      renderCell: (params: GridRenderCellParams<RowData>) =>
        params.row.id && (
          <Link to={`/migration/${params.row.id}`}>View Details →</Link>
        ),
      cellClassName: "name-column--cell",
    },
  ];

  return (
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
      <Box mb="10px"></Box>
      <DataGrid
        rows={formattedResults}
        columns={columns}
        initialState={{
          pagination: {
            paginationModel: {
              pageSize: 10,
            },
          },
        }}
      />
    </Box>
  );
};

export default MigrationDataGrid;
