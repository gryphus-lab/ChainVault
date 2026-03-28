/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { useCallback, useEffect, useRef, useState } from "react";
import type { MigrationEvent } from "@/types";

export function useMigrationEvents() {
  const [events, setEvents] = useState<MigrationEvent[]>([]);
  const [isConnected, setIsConnected] = useState(false);
  const eventSourceRef = useRef<EventSource | null>(null);

  const connect = useCallback(() => {
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
    }

    // Relative URL - important when served from Spring Boot / Thymeleaf
    const url = "/api/migrations/events";
    console.log(`[SSE] Connecting to: ${url}`);

    const eventSource = new EventSource(url);
    eventSourceRef.current = eventSource;

    eventSource.onopen = () => {
      console.log("✅ SSE connected successfully (same origin)");
      setIsConnected(true);
    };

    eventSource.onmessage = (event) => {
      try {
        const newEvent: MigrationEvent = JSON.parse(event.data);
        console.log("📥 Received live migration event:", newEvent);
        setEvents((prev) => [newEvent, ...prev].slice(0, 100));
      } catch (err) {
        console.error("❌ Failed to parse SSE event:", event.data, err);
      }
    };

    eventSource.onerror = (error) => {
      console.error("❌ SSE connection error:", error);
      setIsConnected(false);
      eventSource.close();

      // Auto-reconnect after 3 seconds
      setTimeout(() => {
        console.log("🔄 Reconnecting SSE...");
        // eslint-disable-next-line react-hooks/immutability
        connect();
      }, 3000);
    };

    return () => {
      eventSource.close();
      eventSourceRef.current = null;
    };
  }, []);

  useEffect(() => {
    return connect();
  }, [connect]);

  const clearEvents = () => setEvents([]);
  const reconnect = connect;

  return {
    events,
    isConnected,
    clearEvents,
    reconnect,
  };
}
