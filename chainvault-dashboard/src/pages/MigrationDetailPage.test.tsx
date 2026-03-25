/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter, Route, Routes } from "react-router-dom";

import MigrationDetailPage from "./MigrationDetailPage";

// Mock API
vi.mock("@/lib/api", () => ({
  getMigrationDetail: vi.fn(),
}));

// Mock Timeline (avoid complexity)
vi.mock("@/components/Dashboard/Timeline", () => ({
  default: ({ events }: any) => (
    <div data-testid="timeline">{events.length} events</div>
  ),
}));

import { getMigrationDetail } from "@/lib/api";

const mockMigration = {
  id: "123",
  docId: "doc-123",
  title: "Test Migration",
  status: "SUCCESS",
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString(),
  pageCount: 10,
  traceId: "trace-abc",
  ocrAttempted: true,
  ocrSuccess: true,
  ocrPageCount: 8,
  ocrTotalTextLength: 5000,
  events: [{ id: "e1" }, { id: "e2" }],
  chainZipUrl: "https://example.com/file.zip",
  pdfUrl: "https://example.com/file.pdf",
};

function renderWithRouter(id = "123") {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={[`/migration/${id}`]}>
        <Routes>
          <Route path="/migration/:id" element={<MigrationDetailPage />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe("MigrationDetailPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders loading skeleton", () => {
    (getMigrationDetail as any).mockReturnValue(new Promise(() => {}));

    renderWithRouter();

    // Skeletons render generic divs — just assert something exists
    expect(screen.getAllByRole("generic").length).toBeGreaterThan(0);
  });

  //TODO: Add error state test once implemented

  it("renders migration details", async () => {
    (getMigrationDetail as any).mockResolvedValue(mockMigration);

    renderWithRouter();

    await waitFor(() => {
      expect(screen.getByText("Migration 123")).toBeInTheDocument();
    });

    // Title + metadata
    expect(screen.getByText("Test Migration")).toBeInTheDocument();
    expect(screen.getByText("doc-123")).toBeInTheDocument();
    expect(screen.getByText("trace-abc")).toBeInTheDocument();

    // Status badge
    expect(screen.getByText("SUCCESS")).toBeInTheDocument();

    // Timeline
    expect(screen.getByTestId("timeline")).toHaveTextContent("2 events");
  });

  it("shows OCR information correctly", async () => {
    (getMigrationDetail as any).mockResolvedValue(mockMigration);

    renderWithRouter();

    await screen.findByText("OCR & Processing Details");

    expect(screen.getByText("Yes")).toBeInTheDocument(); // attempted
    expect(screen.getByText("✅ Yes")).toBeInTheDocument(); // success
    expect(screen.getByText("8")).toBeInTheDocument(); // pages processed
    expect(screen.getByText(/5,000 characters/)).toBeInTheDocument();
  });

  it("shows failure reason when present", async () => {
    (getMigrationDetail as any).mockResolvedValue({
      ...mockMigration,
      status: "FAILED",
      failureReason: "Something broke",
    });

    renderWithRouter();

    await screen.findByText("Failure Reason");

    expect(screen.getByText("Something broke")).toBeInTheDocument();
  });

  it("hides failure reason when not present", async () => {
    (getMigrationDetail as any).mockResolvedValue(mockMigration);

    renderWithRouter();

    await screen.findByText("Test Migration");

    expect(screen.queryByText("Failure Reason")).not.toBeInTheDocument();
  });

  it("renders download links when available", async () => {
    (getMigrationDetail as any).mockResolvedValue(mockMigration);

    renderWithRouter();

    await screen.findByText("Downloads");

    expect(screen.getByText("Download Chain ZIP")).toBeInTheDocument();

    expect(screen.getByText("Download Merged PDF")).toBeInTheDocument();
  });

  it("hides download section when no URLs", async () => {
    (getMigrationDetail as any).mockResolvedValue({
      ...mockMigration,
      chainZipUrl: null,
      pdfUrl: null,
    });

    renderWithRouter();

    await screen.findByText("Test Migration");

    expect(screen.queryByText("Downloads")).not.toBeInTheDocument();
  });
});
