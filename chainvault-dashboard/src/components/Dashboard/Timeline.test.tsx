/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import Timeline from "./Timeline";
import type { MigrationEvent } from "@/types";

const mockEvents: MigrationEvent[] = [
  {
    id: "1",
    migrationId: "mig-123",
    timestamp: "2024-01-01T10:00:00Z",
    status: "SUCCESS",
    stepName: "Initialize",
    message: "Starting migration",
    eventType: "TASK_STARTED",
  },
  {
    id: "2",
    migrationId: "mig-345",
    timestamp: "2024-01-01T10:00:01Z", // 1000ms later
    status: "FAILED",
    stepName: "Data Export",
    message: "Connection lost",
    eventType: "TASK_FAILED",
    errorMessage: "Socket Timeout",
    traceId: "trace-123",
  },
];

describe("Timeline Component", () => {
  it("renders loading state correctly", () => {
    render(<Timeline events={[]} isLoading={true} />);
    expect(screen.getByText(/loading timeline/i)).toBeInTheDocument();
  });

  it("renders empty state when no events are provided", () => {
    render(<Timeline events={[]} isLoading={false} />);
    expect(screen.getByText(/no events recorded yet/i)).toBeInTheDocument();
  });

  it("renders a list of events in chronological order", () => {
    // Pass events out of order to test internal sorting
    const outOfOrder = [mockEvents[1], mockEvents[0]];
    render(<Timeline events={outOfOrder} />);

    const items = screen.getAllByRole("listitem");
    expect(items[0]).toHaveTextContent("Initialize");
    expect(items[1]).toHaveTextContent("Data Export");
  });

  it("calculates and displays duration between events", () => {
    render(<Timeline events={mockEvents} />);

    // The second event should show (+1000ms) based on the timestamps
    expect(screen.getByText(/\+1000ms/i)).toBeInTheDocument();
  });

  it("displays error messages and trace IDs when present", () => {
    render(<Timeline events={mockEvents} />);

    expect(screen.getByText("Socket Timeout")).toBeInTheDocument();
    expect(screen.getByText(/Trace: trace-123/i)).toBeInTheDocument();
  });

  it("renders the correct status icons", () => {
    const { container } = render(<Timeline events={mockEvents} />);

    // Check for Lucide icon classes or check SVG existence
    // Since Lucide icons are SVGs, we can look for the specific color classes
    const successIcon = container.querySelector(".text-green-600");
    const failedIcon = container.querySelector(".text-red-600");

    expect(successIcon).toBeInTheDocument();
    expect(failedIcon).toBeInTheDocument();
  });

  it("uses eventType if stepName is missing", () => {
    const eventWithoutStep: MigrationEvent = {
      ...mockEvents[0],
      stepName: undefined,
      eventType: "TASK_STARTED",
    };

    render(<Timeline events={[eventWithoutStep]} />);
    expect(screen.getByText("TASK_STARTED")).toBeInTheDocument();
  });
});
