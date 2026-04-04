/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { renderHook, act } from "@testing-library/react";
import { useMigrationEvents } from "./useMigrationEvents";

/* eslint-disable @typescript-eslint/no-explicit-any */
// --- Mock EventSource ---
class MockEventSource {
  url: string;
  onopen: (() => void) | null = null;
  onmessage: ((event: MessageEvent) => void) | null = null;
  onerror: ((err: any) => void) | null = null;
  close = vi.fn();

  constructor(url: string) {
    this.url = url;
    MockEventSource.instances.push(this);
  }

  static instances: MockEventSource[] = [];
}

(global as any).EventSource = MockEventSource;

describe("useMigrationEvents", () => {
  beforeEach(() => {
    vi.useFakeTimers(); // Essential for testing the 3000ms delay
    MockEventSource.instances = [];
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it("attempts to auto-reconnect after 3 seconds on error", () => {
    renderHook(() => useMigrationEvents());
    const firstInstance = MockEventSource.instances[0];

    act(() => {
      firstInstance.onerror?.(new Error("Connection lost"));
    });

    expect(MockEventSource.instances.length).toBe(1);

    // Fast-forward 3 seconds
    act(() => {
      vi.advanceTimersByTime(3000);
    });

    // A second instance should have been created
    expect(MockEventSource.instances.length).toBe(2);
    expect(MockEventSource.instances[1].url).toBe("/api/migrations/events");
  });

  it("updates connection status and logs success on open", () => {
    const consoleSpy = vi.spyOn(console, "log").mockImplementation(() => {});
    const { result } = renderHook(() => useMigrationEvents());
    const instance = MockEventSource.instances[0];

    act(() => {
      instance.onopen?.();
    });

    // Verifies setIsConnected(true)
    expect(result.current.isConnected).toBe(true);

    // Verifies the specific console.log inside onopen
    expect(consoleSpy).toHaveBeenCalledWith("✅ SSE connected successfully");

    consoleSpy.mockRestore();
  });

  it("replaces existing connection when manual reconnect is called", () => {
    const { result } = renderHook(() => useMigrationEvents());
    const firstInstance = MockEventSource.instances[0];

    act(() => {
      result.current.reconnect();
    });

    expect(firstInstance.close).toHaveBeenCalled();
    expect(MockEventSource.instances.length).toBe(2);
  });

  it("ignores events missing required fields (id or createdAt)", () => {
    const warnSpy = vi.spyOn(console, "warn").mockImplementation(() => {});
    const { result } = renderHook(() => useMigrationEvents());
    const instance = MockEventSource.instances[0];

    act(() => {
      // Missing createdAt
      instance.onmessage?.({
        data: JSON.stringify({ id: "1", message: "Oops" }),
      } as MessageEvent);
    });

    expect(result.current.events.length).toBe(0);
    expect(warnSpy).toHaveBeenCalledWith(
      expect.stringContaining("Received incomplete event"),
      expect.anything(),
    );
    warnSpy.mockRestore();
  });

  it("prepends new events to the start of the list", () => {
    const { result } = renderHook(() => useMigrationEvents());
    const instance = MockEventSource.instances[0];

    const event1 = {
      id: "1",
      timestamp: "2026-01-01T10:00:00Z",
      message: "First",
    };
    const event2 = {
      id: "2",
      timestamp: "2026-01-01T10:05:00Z",
      message: "Second",
    };

    act(() => {
      instance.onmessage?.({ data: JSON.stringify(event1) } as MessageEvent);
    });
    act(() => {
      instance.onmessage?.({ data: JSON.stringify(event2) } as MessageEvent);
    });

    // Latest event should be at index 0
    expect(result.current.events[0].id).toBe("2");
    expect(result.current.events[1].id).toBe("1");
  });

  it("strictly enforces the 100 event limit", () => {
    const { result } = renderHook(() => useMigrationEvents());
    const instance = MockEventSource.instances[0];

    act(() => {
      // Send 110 events
      for (let i = 0; i < 110; i++) {
        instance.onmessage?.({
          data: JSON.stringify({
            id: `${i}`,
            timestamp: new Date().toISOString(),
            message: `Event ${i}`,
          }),
        } as MessageEvent);
      }
    });

    expect(result.current.events.length).toBe(100);
    expect(result.current.events.find((e) => e.id === "0")).toBeUndefined();
    expect(result.current.events[0].id).toBe("109");
  });

  it("ignores empty data frames", () => {
    const { result } = renderHook(() => useMigrationEvents());
    const instance = MockEventSource.instances[0];

    act(() => {
      instance.onmessage?.({ data: "   " } as MessageEvent);
    });

    expect(result.current.events.length).toBe(0);
  });

  it("logs error when JSON parsing fails (catch block)", () => {
    const consoleErrorSpy = vi
      .spyOn(console, "error")
      .mockImplementation(() => {});
    const { result } = renderHook(() => useMigrationEvents());
    const instance = MockEventSource.instances[0];
    const malformedData = "{ invalid: json }";

    act(() => {
      instance.onmessage?.({
        data: malformedData,
      } as MessageEvent);
    });

    // Verifies the catch block was hit
    expect(consoleErrorSpy).toHaveBeenCalledWith(
      "❌ Failed to parse SSE event. Raw data:",
      malformedData,
      expect.any(SyntaxError), // The error object thrown by JSON.parse
    );

    // Verify state wasn't updated with bad data
    expect(result.current.events).toEqual([]);

    consoleErrorSpy.mockRestore();
  });
});
