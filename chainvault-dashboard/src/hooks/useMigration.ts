/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { useCallback, useEffect, useState } from "react";
import type { MigrationEvent } from "@/types";

export function useMigrationEvents() {
  const [events, setEvents] = useState<MigrationEvent[]>([]);
  const [isConnected, setIsConnected] = useState(false);

  const connect = useCallback(() => {
    const eventSource = new EventSource("/api/migrations/events");

    eventSource.onopen = () => {
      setIsConnected(true);
      console.log("✅ SSE connected");
    };

    eventSource.onmessage = (event) => {
      try {
        const newEvent: MigrationEvent = JSON.parse(event.data);
        setEvents((prev) => [newEvent, ...prev].slice(0, 50)); // keep latest 50
      } catch (err) {
        console.error("Failed to parse SSE event:", err);
      }
    };

    eventSource.onerror = (err) => {
      console.error("SSE Error:", err);
      setIsConnected(false);
      eventSource.close();
    };

    return () => {
      eventSource.close();
    };
  }, []);

  useEffect(() => {
    return connect();
  }, [connect]);

  const clearEvents = () => setEvents([]);

  return { events, isConnected, clearEvents };
}
