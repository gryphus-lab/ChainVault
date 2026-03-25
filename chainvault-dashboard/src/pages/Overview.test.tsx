/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";

import Overview from "./Overview";

// Mock API
vi.mock("@/lib/api", () => ({
  getMigrations: vi.fn(),
  getMigrationStats: vi.fn(),
}));

import { getMigrations, getMigrationStats } from "@/lib/api";

const mockStats = {
  total: 3,
  success: 1,
  failed: 1,
  running: 1,
  pending: 0,
};

const mockMigrations = [
  {
    id: "abc",
    docId: "doc-abc",
    title: "First Migration",
    status: "SUCCESS",
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
  {
    id: "def",
    docId: "doc-def",
    title: "Second Migration",
    status: "FAILED",
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
  {
    id: "xyz",
    docId: "doc-xyz",
    title: "Running Job",
    status: "RUNNING",
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
];

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

describe("Overview", () => {
  beforeEach(() => {
    vi.clearAllMocks();

    (getMigrationStats as any).mockResolvedValue(mockStats);
    (getMigrations as any).mockResolvedValue(mockMigrations);
  });

  it("shows loading state initially", () => {
    (getMigrationStats as any).mockReturnValue(new Promise(() => {}));
    (getMigrations as any).mockReturnValue(new Promise(() => {}));

    renderComponent();

    expect(screen.getAllByRole("generic").length).toBeGreaterThan(0);
  });

  it("renders stats and migrations", async () => {
    renderComponent();

    await waitFor(() => {
      expect(screen.getByText("Migration Dashboard")).toBeInTheDocument();
    });

    // Stats
    expect(screen.getByText("3")).toBeInTheDocument(); // total
    expect(screen.getAllByText("1")).toHaveLength(3); // success/failed/running

    // Table rows
    expect(screen.getByText("First Migration")).toBeInTheDocument();
    expect(screen.getByText("Second Migration")).toBeInTheDocument();
    expect(screen.getByText("Running Job")).toBeInTheDocument();
  });

  it("filters by search term", async () => {
    renderComponent();

    await screen.findByText("First Migration");

    const input = screen.getByPlaceholderText("Search by Doc ID or Title...");

    fireEvent.change(input, { target: { value: "second" } });

    expect(screen.queryByText("First Migration")).not.toBeInTheDocument();
    expect(screen.getByText("Second Migration")).toBeInTheDocument();
  });

  it("filters by status", async () => {
    renderComponent();

    await screen.findByText("First Migration");

    const select = screen.getByDisplayValue("All Statuses");

    fireEvent.change(select, { target: { value: "FAILED" } });

    expect(screen.getByText("Second Migration")).toBeInTheDocument();
    expect(screen.queryByText("First Migration")).not.toBeInTheDocument();
  });

  it("shows empty state when no results", async () => {
    renderComponent();

    await screen.findByText("First Migration");

    const input = screen.getByPlaceholderText("Search by Doc ID or Title...");

    fireEvent.change(input, { target: { value: "non-existent" } });

    expect(
      screen.getByText("No migrations found matching your filters."),
    ).toBeInTheDocument();
  });

  it("clears filters when clicking clear button", async () => {
    renderComponent();

    await screen.findByText("First Migration");

    const input = screen.getByPlaceholderText("Search by Doc ID or Title...");

    fireEvent.change(input, { target: { value: "second" } });

    const clearButton = screen.getByText("Clear");
    fireEvent.click(clearButton);

    expect(input).toHaveValue("");
    expect(screen.getByText("First Migration")).toBeInTheDocument();
    expect(screen.getByText("Second Migration")).toBeInTheDocument();
  });
});
