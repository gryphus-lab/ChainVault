/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { Box } from '@mui/material'
import StatBox from '@/components/StatBox' // Ensure this matches your actual export path
import SvgIcon from '@mui/icons-material/Circle'

interface MigrationStats {
  total?: number
  success?: number
  pending?: number
  running?: number
  failed?: number
}

interface OverviewDataBoxProps {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  colors: any // Ideally use your theme's palette type
  stats?: MigrationStats
}

const OverviewDataBox = ({ colors, stats }: OverviewDataBoxProps) => {
  // Shared styles for the grid items
  const commonBoxStyles = {
    gridColumn: 'span 3',
    backgroundColor: colors.primary[400],
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    sx: { cursor: 'pointer' },
  }

  return (
    <>
      {/* Total Migrations */}
      <Box {...commonBoxStyles}>
        <StatBox
          title={stats?.total?.toString() ?? '0'}
          subtitle="Total Migrations"
          icon={<SvgIcon sx={{ color: colors.greenAccent[600], fontSize: '26px' }} />}
        />
      </Box>

      {/* Successful */}
      <Box {...commonBoxStyles}>
        <StatBox
          title={stats?.success?.toString() ?? '0'}
          subtitle="Successful"
          icon={<SvgIcon sx={{ color: colors.greenAccent[600], fontSize: '26px' }} />}
        />
      </Box>

      {/* In Progress (Pending + Running) */}
      <Box {...commonBoxStyles}>
        <StatBox
          title={((stats?.pending ?? 0) + (stats?.running ?? 0)).toString()}
          subtitle="In Progress"
          icon={<SvgIcon sx={{ color: colors.orangeAccent[600], fontSize: '26px' }} />}
        />
      </Box>

      {/* Failed */}
      <Box {...commonBoxStyles}>
        <StatBox
          title={stats?.failed?.toString() ?? '0'}
          subtitle="Failed"
          icon={<SvgIcon sx={{ color: colors.redAccent[600], fontSize: '26px' }} />}
        />
      </Box>
    </>
  )
}

export default OverviewDataBox
