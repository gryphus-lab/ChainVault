/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";

import App from "./App";

// --- mock pages (IMPORTANT) ---
vi.mock("@/pages/Overview", () => ({
  default: () => <div>Overview Page</div>,
}));

vi.mock("@/pages/MigrationDetailPage", () => ({
  default: () => <div>Migration Detail Page</div>,
}));

describe("App routing", () => {
  it("renders Overview on /", () => {
    render(
      <MemoryRouter initialEntries={["/"]}>
        <App />
      </MemoryRouter>,
    );

    expect(screen.getByText("Overview Page")).toBeInTheDocument();
  });

  it("renders MigrationDetailPage on /migration/:id", () => {
    render(
      <MemoryRouter initialEntries={["/migration/123"]}>
        <App />
      </MemoryRouter>,
    );

    expect(screen.getByText("Migration Detail Page")).toBeInTheDocument();
  });

  it("renders correct page when navigating directly", () => {
    render(
      <MemoryRouter initialEntries={["/migration/999"]}>
        <App />
      </MemoryRouter>,
    );

    expect(screen.getByText("Migration Detail Page")).toBeInTheDocument();
  });
});
