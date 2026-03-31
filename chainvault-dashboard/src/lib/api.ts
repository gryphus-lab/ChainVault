/*
 * Copyright (c) 2026. Gryphus Lab
 */
import ky from "ky";
import { MigrationDetail } from "@/types";

const api = ky.create({
  prefixUrl: "/api",
  timeout: 15000,
  retry: {
    limit: 2,
    methods: ["get", "post", "put", "delete"],
  },
  headers: {
    "Content-Type": "application/json",
  },
});

/* eslint-disable @typescript-eslint/no-explicit-any */
// Explicitly typed functions
export const getMigrations = async (params?: {
  limit?: number;
}): Promise<any[]> => {
  return api.get("migrations", { searchParams: params }).json();
};

export const getMigrationStats = async (): Promise<any> => {
  return api.get("migrations/stats").json();
};

export const getMigrationDetail = async (
  id: string,
): Promise<MigrationDetail> => {
  return api.get(`migrations/${id}`).json();
};

export const getMigrationEvents = async (
  migrationId: string,
): Promise<any[]> => {
  return api.get(`migrations/${migrationId}/events`).json();
};

export { api };
