/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, it, expect, vi } from "vitest";
import Dashboard from "./index";

// 1. Mock the custom theme tokens
vi.mock("@/theme", () => ({
  tokens: () => ({
    grey: { 100: "#f0f0f0" },
    // Add other colors used by StatisticsPanel if necessary
  }),
}));

// 2. Mock sub-components to keep tests isolated and fast
vi.mock("@/components", () => ({
  Header: ({ title, subtitle }: { title: string; subtitle: string }) => (
    <div data-testid="mock-header">
      {title} - {subtitle}
    </div>
  ),
}));

vi.mock("@/scenes/dashboard/statisticsPanel", () => ({
  default: () => <div data-testid="statistics-panel">Stats</div>,
}));

vi.mock("@/pages/Overview", () => ({
  default: () => <div data-testid="overview-page">Overview</div>,
}));

vi.mock("@/pages/MigrationDetailPage", () => ({
  default: () => <div data-testid="details-page">Migration Details</div>,
}));

describe("Dashboard Component", () => {
  const renderDashboard = (initialRoute = "/") => {
    return render(
      <MemoryRouter initialEntries={[initialRoute]}>
        <Dashboard />
      </MemoryRouter>,
    );
  };

  it("renders the Header with correct title and subtitle", () => {
    renderDashboard();
    expect(
      screen.getByText(/Chainvault - Migration Dashboard/i),
    ).toBeInTheDocument();
  });

  it("renders the StatisticsPanel", () => {
    renderDashboard();
    expect(screen.getByTestId("statistics-panel")).toBeInTheDocument();
  });

  it("renders the Overview page on the default route", () => {
    renderDashboard("/");
    expect(screen.getByTestId("overview-page")).toBeInTheDocument();
  });

  it("renders the MigrationDetailPage when navigating to a specific ID", () => {
    renderDashboard("/migration/123");
    expect(screen.getByTestId("details-page")).toBeInTheDocument();
  });
});
