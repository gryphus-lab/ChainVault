/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { Box, useTheme } from '@mui/material'

import { Header } from '@/components'
import { tokens } from '@/theme'
import { Route, Routes } from 'react-router-dom'
import Overview from '@/pages/Overview'
import MigrationDetailPage from '@/pages/MigrationDetailPage'
import StatisticsPanel from '@/scenes/dashboard/statisticsPanel'

function Dashboard() {
  const theme = useTheme()
  const colors = tokens(theme.palette.mode)

  return (
    <Box m="20px">
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Header title="Chainvault" subtitle="Migration Dashboard" />
      </Box>

      <Box display="grid" gridTemplateColumns={'repeat(12, 1fr)'} gridAutoRows="140px" gap="20px">
        <StatisticsPanel colors={colors} />
      </Box>

      <Box sx={{ flex: 1 }}>
        <Routes>
          <Route path="/" element={<Overview />} />
          <Route path="/migration/:id" element={<MigrationDetailPage />} />
        </Routes>
      </Box>
    </Box>
  )
}

export default Dashboard
