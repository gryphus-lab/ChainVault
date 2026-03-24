/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { useQuery } from "@tanstack/react-query";
import {
  getMigrationDetail,
  getMigrations,
  getMigrationStats,
} from "@/lib/api";
import type { Migration, MigrationStats, MigrationDetail } from "@/types";

export function useMigrationStats() {
  return useQuery<MigrationStats>({
    queryKey: ["migration-stats"],
    queryFn: getMigrationStats,
    staleTime: 30 * 1000,
  });
}

export function useMigrations() {
  return useQuery<Migration[]>({
    queryKey: ["migrations"],
    queryFn: () => getMigrations({ limit: 50 }),
    staleTime: 60 * 1000,
  });
}

export function useMigrationDetail(id: string) {
  return useQuery<MigrationDetail>({
    queryKey: ["migration-detail", id],
    queryFn: () => getMigrationDetail(id),
    enabled: !!id,
    staleTime: 30 * 1000,
  });
}
