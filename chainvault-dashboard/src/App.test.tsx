/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { describe, it, expect, vi } from "vitest";
import { screen, render } from "@/test/test-utils"; // ← Use our custom render
import App from "./App";

// Mock API calls
vi.mock("@/lib/api", () => ({
  getMigrationStats: vi.fn().mockResolvedValue({
    total: 42,
    pending: 5,
    running: 3,
    success: 28,
    failed: 4,
    compensated: 2,
    last24h: 12,
  }),
  getMigrations: vi.fn().mockResolvedValue([
    {
      id: "DOC-INV-2026-001",
      docId: "DOC-TEST-001",
      title: "Invoice #8742 - Acme Solutions AG",
      status: "SUCCESS",
      createdAt: "2026-03-24T10:15:30Z",
      updatedAt: "2026-03-24T10:18:45Z",
      pageCount: 5,
    },
  ]),
}));

describe("App Component", () => {
  it("renders dashboard title", () => {
    render(<App />);
    expect(
      screen.getByText(/ChainVault Migration Dashboard/i),
    ).toBeInTheDocument();
  });

  it("renders stats cards", async () => {
    render(<App />);
    expect(await screen.findByText("Total Migrations")).toBeInTheDocument();
    expect(await screen.findByText("Successful")).toBeInTheDocument();
  });

  it("renders recent migrations table", async () => {
    render(<App />);
    expect(await screen.findByText("Recent Migrations")).toBeInTheDocument();
    expect(await screen.findByText("DOC-INV-2026-001")).toBeInTheDocument();
  });
});
