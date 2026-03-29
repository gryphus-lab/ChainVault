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

    const url = "/api/migrations/events";
    console.log(`[SSE] Connecting to: ${url}`);

    const eventSource = new EventSource(url);
    eventSourceRef.current = eventSource;

    eventSource.onopen = () => {
      console.log("✅ SSE connected successfully");
      setIsConnected(true);
    };

    eventSource.onmessage = (event) => {
      try {
        const rawData = event.data.trim();
        console.log("📥 Raw SSE data received:", rawData);

        if (!rawData) return;

        const newEvent: MigrationEvent = JSON.parse(rawData);

        if (!newEvent?.id || !newEvent?.timestamp) {
          console.warn("⚠️ Received incomplete event:", newEvent);
          return;
        }

        console.log(
          "✅ Successfully parsed event:",
          newEvent.eventType,
          newEvent.message,
        );

        setEvents((prev) => [newEvent, ...prev].slice(0, 100));
      } catch (err) {
        console.error(
          "❌ Failed to parse SSE event. Raw data:",
          event.data,
          err,
        );
      }
    };

    eventSource.onerror = (error) => {
      console.error("❌ SSE connection error:", error);
      setIsConnected(false);
      eventSource.close();

      // Auto-reconnect
      setTimeout(() => {
        console.log("🔄 Attempting SSE reconnect...");
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

  return { events, isConnected, clearEvents, reconnect };
}
