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
  it("renders a list of events in chronological order", () => {
    // Pass events out of order to test internal sorting
    const outOfOrder = [mockEvents[1], mockEvents[0]];
    render(<Timeline events={outOfOrder} />);

    const items = screen.getAllByRole("listitem");
    expect(items[0]).toHaveTextContent("Initialize");
    expect(items[1]).toHaveTextContent("Data Export");
  });

  it("displays error messages and trace IDs when present", () => {
    render(<Timeline events={mockEvents} />);

    expect(screen.getByText("Socket Timeout")).toBeInTheDocument();
    expect(screen.getByText(/Trace ID: trace-123/i)).toBeInTheDocument();
  });

  it("renders the correct status icons", () => {
    const { container } = render(<Timeline events={mockEvents} />);
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
    expect(screen.getByText("TASK STARTED")).toBeInTheDocument();
  });

  it("renders message when no events are available", () => {
    render(<Timeline events={[]} />);
    expect(
      screen.getByText("No timeline events available yet."),
    ).toBeInTheDocument();
  });
});
