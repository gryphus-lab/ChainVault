/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import axios from "axios";
import * as api from "./api";

// Mock the entire axios module
vi.mock("axios", () => {
  return {
    default: {
      create: vi.fn().mockReturnThis(),
      get: vi.fn(),
    },
  };
});

describe("API Service", () => {
  const mockAxiosGet = vi.mocked(axios.get);

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("getMigrations calls the correct endpoint with params", async () => {
    const mockData = [{ id: "1", status: "completed" }];
    mockAxiosGet.mockResolvedValueOnce({ data: mockData });

    const params = { status: "pending", limit: 10 };
    const result = await api.getMigrations(params);

    expect(mockAxiosGet).toHaveBeenCalledWith("/api/migrations", { params });
    expect(result).toEqual(mockData);
  });

  it("getMigrationById calls the correct dynamic endpoint", async () => {
    const mockData = { id: "123", name: "Test Migration" };
    mockAxiosGet.mockResolvedValueOnce({ data: mockData });

    const result = await api.getMigrationById("123");

    expect(mockAxiosGet).toHaveBeenCalledWith("/api/migrations/123");
    expect(result).toEqual(mockData);
  });

  it("getMigrationStats returns stats data", async () => {
    const mockStats = { total: 100, failed: 5 };
    mockAxiosGet.mockResolvedValueOnce({ data: mockStats });

    const result = await api.getMigrationStats();

    expect(mockAxiosGet).toHaveBeenCalledWith("/api/migrations/stats");
    expect(result).toEqual(mockStats);
  });

  it("getMigrationDetail calls the detail endpoint", async () => {
    const mockDetail = { id: "123", logs: [] };
    mockAxiosGet.mockResolvedValueOnce({ data: mockDetail });

    const result = await api.getMigrationDetail("123");

    expect(mockAxiosGet).toHaveBeenCalledWith("/api/migrations/123/detail");
    expect(result).toEqual(mockDetail);
  });

  it("handles API errors gracefully", async () => {
    mockAxiosGet.mockRejectedValueOnce(new Error("Network Error"));

    await expect(api.getMigrations()).rejects.toThrow("Network Error");
  });
});
