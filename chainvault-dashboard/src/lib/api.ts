/*
 * Copyright (c) 2026. Gryphus Lab
 */
import axios from "axios";
import type { Migration, MigrationStats, MigrationDetail } from "@/types";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
});

export const getMigrations = async (params?: {
  status?: string;
  limit?: number;
  offset?: number;
}): Promise<Migration[]> => {
  const res = await api.get("/api/migrations", { params });
  return res.data;
};

export const getMigrationById = async (id: string): Promise<Migration> => {
  const res = await api.get(`/api/migrations/${id}`);
  return res.data;
};

export const getMigrationStats = async (): Promise<MigrationStats> => {
  const res = await api.get("/api/migrations/stats");
  return res.data;
};

export const getMigrationDetail = async (
  id: string,
): Promise<MigrationDetail> => {
  const res = await api.get(`/api/migrations/${id}/detail`);
  return res.data;
};
