/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
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
    MockEventSource.instances = [];
    vi.clearAllMocks();
  });

  it("connects to SSE and sets isConnected on open", () => {
    const { result } = renderHook(() => useMigrationEvents());

    const instance = MockEventSource.instances[0];

    act(() => {
      instance.onopen?.();
    });

    expect(result.current.isConnected).toBe(true);
  });

  it("receives and stores events", () => {
    const { result } = renderHook(() => useMigrationEvents());

    const instance = MockEventSource.instances[0];

    const mockEvent = {
      migrationId: "1",
      status: "SUCCESS",
      timestamp: new Date().toISOString(),
    };

    act(() => {
      instance.onmessage?.({
        data: JSON.stringify(mockEvent),
      } as MessageEvent);
    });

    expect(result.current.events.length).toBe(1);
    expect(result.current.events[0]).toEqual(mockEvent);
  });

  it("keeps only latest 60 events", () => {
    const { result } = renderHook(() => useMigrationEvents());

    const instance = MockEventSource.instances[0];

    act(() => {
      for (let i = 0; i < 60; i++) {
        instance.onmessage?.({
          data: JSON.stringify({
            migrationId: `${i}`,
            status: "SUCCESS",
            timestamp: new Date().toISOString(),
          }),
        } as MessageEvent);
      }
    });

    expect(result.current.events.length).toBe(60);
  });

  it("handles invalid JSON safely", () => {
    const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});

    renderHook(() => useMigrationEvents());

    const instance = MockEventSource.instances[0];

    act(() => {
      instance.onmessage?.({
        data: "invalid-json",
      } as MessageEvent);
    });

    expect(consoleSpy).toHaveBeenCalled();

    consoleSpy.mockRestore();
  });

  it("handles error and disconnects", () => {
    const { result } = renderHook(() => useMigrationEvents());

    const instance = MockEventSource.instances[0];

    act(() => {
      instance.onopen?.();
    });

    expect(result.current.isConnected).toBe(true);

    act(() => {
      instance.onerror?.(new Error());
    });

    expect(result.current.isConnected).toBe(false);
    expect(instance.close).toHaveBeenCalled();
  });

  it("clears events", () => {
    const { result } = renderHook(() => useMigrationEvents());

    const instance = MockEventSource.instances[0];

    act(() => {
      instance.onmessage?.({
        data: JSON.stringify({
          migrationId: "1",
          status: "SUCCESS",
          timestamp: new Date().toISOString(),
        }),
      } as MessageEvent);
    });

    expect(result.current.events.length).toBe(1);

    act(() => {
      result.current.clearEvents();
    });

    expect(result.current.events).toEqual([]);
  });

  it("closes connection on unmount", () => {
    const { unmount } = renderHook(() => useMigrationEvents());

    const instance = MockEventSource.instances[0];

    unmount();

    expect(instance.close).toHaveBeenCalled();
  });
});
