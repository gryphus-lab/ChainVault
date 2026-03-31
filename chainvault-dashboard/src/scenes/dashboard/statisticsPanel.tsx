/*
 * Copyright (c) 2026. Gryphus Lab
 */
import OverviewDataBox from "@/scenes/dashboard/overviewDataBox";
import { getMigrationStats } from "@/lib/api";
import { useQuery } from "@tanstack/react-query";

/* eslint-disable @typescript-eslint/no-explicit-any */
interface StatisticsPanelProps {
  colors: any; // Ideally, replace 'any' with your Theme colors type
}

const StatisticsPanel = ({ colors }: StatisticsPanelProps) => {
  const { data: stats } = useQuery({
    queryKey: ["migration-stats"],
    queryFn: getMigrationStats,
  });

  return <OverviewDataBox colors={colors} stats={stats} />;
};

export default StatisticsPanel;
