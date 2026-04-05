/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { Box, Typography, useTheme } from '@mui/material'
import { tokens } from '@/theme'

const Header = ({ title = '', subtitle = '' }) => {
  const theme = useTheme()
  const colors = tokens(theme.palette.mode)

  return (
    <Box mb="30px">
      <Typography
        variant="h2"
        color={colors.gray[100]}
        fontWeight="bold"
        sx={{ mb: '5px' }} // Using sx for consistency
      >
        {title}
      </Typography>
      <Typography variant="h5" color={colors.greenAccent[400]}>
        {subtitle}
      </Typography>
    </Box>
  )
}

export default Header
