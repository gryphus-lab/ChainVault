/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";

import Overview from "./Overview";

// --- mocks ---
vi.mock("@/lib/api", () => ({
  getMigrations: vi.fn(),
  getMigrationStats: vi.fn(),
}));

vi.mock("@/hooks/useMigrationEvents", () => ({
  useMigrationEvents: vi.fn(),
}));

import { getMigrations, getMigrationStats } from "@/lib/api";
import { useMigrationEvents } from "@/hooks/useMigrationEvents.ts";

// --- test helpers ---
function renderComponent() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <Overview />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

// --- mock data ---
const mockStats = {
  total: 2,
  success: 1,
  failed: 1,
  running: 0,
  pending: 0,
};

const baseMigrations = [
  {
    id: "1",
    docId: "doc-1",
    title: "First Migration",
    status: "PENDING",
    createdAt: new Date(Date.now() - 60000).toISOString(),
    updatedAt: new Date(Date.now() - 60000).toISOString(),
  },
  {
    id: "2",
    docId: "doc-2",
    title: "Second Migration",
    status: "FAILED",
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
  {
    id: "really-old",
    docId: "doc-really-old",
    title: "Really Old Migration",
    status: "SUCCESS",
    createdAt: new Date(Date.now() - 3000000000).toISOString(),
    updatedAt: new Date(Date.now() - 3000000000).toISOString(),
  },
];

/* eslint-disable @typescript-eslint/no-explicit-any */
describe("Overview (with live events)", () => {
  beforeEach(() => {
    vi.clearAllMocks();

    (getMigrationStats as any).mockResolvedValue(mockStats);
    (getMigrations as any).mockResolvedValue(baseMigrations);

    (useMigrationEvents as any).mockReturnValue({
      events: [],
      isConnected: true,
      clearEvents: vi.fn(),
    });
  });

  it("shows live connection status", async () => {
    renderComponent();

    await screen.findByText("Migration Dashboard");

    expect(screen.getByText("Live • Connected")).toBeInTheDocument();
  });

  it("shows disconnected state", async () => {
    (useMigrationEvents as any).mockReturnValue({
      events: [],
      isConnected: false,
      clearEvents: vi.fn(),
    });

    renderComponent();

    await screen.findByText("Disconnected");

    expect(screen.getByText("Disconnected")).toBeInTheDocument();
  });

  it("calls clearEvents when clicking Clear Live", async () => {
    const clearEvents = vi.fn();

    (useMigrationEvents as any).mockReturnValue({
      events: [],
      isConnected: true,
      clearEvents,
    });

    renderComponent();

    await screen.findByText("Migration Dashboard");

    fireEvent.click(screen.getByText("Clear Events"));

    expect(clearEvents).toHaveBeenCalled();
  });

  it("filters by search", async () => {
    renderComponent();

    await screen.findByText("First Migration");

    fireEvent.change(
      screen.getByPlaceholderText("Search by Doc ID or Title..."),
      { target: { value: "second" } },
    );

    expect(screen.getByText("Second Migration")).toBeInTheDocument();
    expect(screen.queryByText("First Migration")).not.toBeInTheDocument();
  });

  it("filters by status", async () => {
    renderComponent();

    await screen.findByText("First Migration");

    fireEvent.change(screen.getByDisplayValue("All Statuses"), {
      target: { value: "FAILED" },
    });

    expect(screen.getByText("Second Migration")).toBeInTheDocument();
    expect(screen.queryByText("First Migration")).not.toBeInTheDocument();
  });

  it("filters by date", async () => {
    renderComponent();

    await screen.findByText("First Migration");

    const dateFilter = ["24h", "7d", "30d"];
    const AllTimeFilter = screen.getByDisplayValue("All Time");

    for (const filter of dateFilter) {
      fireEvent.change(AllTimeFilter, {
        target: { value: filter },
      });

      expect(screen.getByText("First Migration")).toBeInTheDocument();
      expect(screen.getByText("Second Migration")).toBeInTheDocument();

      expect(
        screen.queryByText("Really Old Migration"),
      ).not.toBeInTheDocument(); // older than 30d should never be displayed
    }
  });

  it("shows empty state when no results", async () => {
    renderComponent();

    await screen.findByText("First Migration");

    fireEvent.change(
      screen.getByPlaceholderText("Search by Doc ID or Title..."),
      { target: { value: "zzz" } },
    );

    expect(
      screen.getByText("No migrations found matching your filters."),
    ).toBeInTheDocument();
  });

  it("applies live event updates to migrations", async () => {
    const now = new Date().toISOString();

    (useMigrationEvents as any).mockReturnValue({
      events: [
        {
          migrationId: "1",
          status: "SUCCESS",
          timestamp: now,
        },
      ],
      isConnected: true,
      clearEvents: vi.fn(),
    });

    renderComponent();

    await screen.findByText("First Migration");

    // status should be updated from PENDING → SUCCESS
    expect(screen.getAllByText("SUCCESS").length).toBeGreaterThan(0);
  });

  it("sorts by updatedAt (live events take priority)", async () => {
    const older = new Date(Date.now() - 100000).toISOString();
    const newer = new Date().toISOString();

    (getMigrations as any).mockResolvedValue([
      {
        ...baseMigrations[0],
        createdAt: older,
        updatedAt: older,
      },
      {
        ...baseMigrations[1],
        createdAt: older,
        updatedAt: older,
      },
    ]);

    (useMigrationEvents as any).mockReturnValue({
      events: [
        {
          migrationId: "2",
          status: "FAILED",
          timestamp: newer,
        },
      ],
      isConnected: true,
      clearEvents: vi.fn(),
    });

    renderComponent();

    await screen.findByText("First Migration");

    const rows = screen.getAllByRole("row");

    // crude but effective: first data row should contain updated migration
    expect(rows[1]).toHaveTextContent("Second Migration");
  });
});
