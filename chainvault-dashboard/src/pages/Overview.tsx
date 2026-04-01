/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { format, parseISO, subDays } from "date-fns";
import { Clock, Search } from "lucide-react";

import { getMigrations } from "@/lib/api";
import { useMigrationEvents } from "@/hooks/useMigrationEvents";

import { Badge } from "@/components/ui/Badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/Card";
import MigrationDataGrid from "@/scenes/dashboard/migrationDataGrid";

type StatusFilter = "ALL" | "SUCCESS" | "FAILED" | "RUNNING" | "PENDING";

export default function Overview() {
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState<StatusFilter>("ALL");
  const [dateFilter, setDateFilter] = useState<"all" | "24h" | "7d" | "30d">(
    "all",
  );

  const {
    data: allMigrations = [],
    isLoading: migrationsLoading,
    error: migrationsError,
  } = useQuery({
    queryKey: ["migrations"],
    queryFn: async () => {
      const data = await getMigrations({ limit: 100 });
      return Array.isArray(data) ? data : [];
    },
    retry: 2,
  });

  const {
    events: liveEvents,
    isConnected,
    clearEvents,
    reconnect,
  } = useMigrationEvents();

  // Merge live updates
  const migrationsWithLive = useMemo(() => {
    const merged = [...allMigrations];
    liveEvents.forEach((liveEvent) => {
      if (!liveEvent?.migrationId) return;
      const index = merged.findIndex((m) => m.id === liveEvent.migrationId);
      if (index !== -1) {
        merged[index] = {
          ...merged[index],
          updatedAt: liveEvent.createdAt || merged[index].updatedAt,
        };
      }
    });
    return merged;
  }, [allMigrations, liveEvents]);

  const filteredMigrations = useMemo(() => {
    let result = [...migrationsWithLive];

    if (statusFilter !== "ALL") {
      result = result.filter((m) => m.status === statusFilter);
    }

    if (dateFilter !== "all") {
      let days: number;
      switch (dateFilter) {
        case "7d":
          days = 7;
          break;
        case "24h":
          days = 1;
          break;
        default:
          days = 30;
          break;
      }
      const cutoff = subDays(new Date(), days);
      result = result.filter((m) => new Date(m.createdAt || 0) >= cutoff);
    }

    // Search term (docId or title)
    if (searchTerm.trim()) {
      const term = searchTerm.toLowerCase().trim();
      result = result.filter(
        (m) =>
          (m.docId || "").toLowerCase().includes(term) ||
          (m.title || "").toLowerCase().includes(term),
      );
    }

    return result.sort(
      (a, b) =>
        new Date(b.updatedAt || b.createdAt || 0).getTime() -
        new Date(a.updatedAt || a.createdAt || 0).getTime(),
    );
  }, [migrationsWithLive, statusFilter, dateFilter, searchTerm]);

  function getMigrationStatus() {
    if (migrationsLoading) {
      return "Loading migrations...";
    } else if (migrationsError) {
      return "Error loading migrations";
    } else {
      return "No migrations found yet.";
    }
  }

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex justify-between">
        <div className="flex gap-3">
          <div
            className={`flex gap-2 px-4 py-1.5 rounded-full text-sm font-medium ${isConnected ? "bg-emerald-100 text-emerald-700" : "bg-red-100 text-red-700"}`}
          >
            <div
              className={`w-2.5 h-2.5 rounded-full ${isConnected ? "bg-emerald-500 animate-pulse" : "bg-red-500"}`}
            />
            {isConnected ? "Live • Connected" : "Disconnected"}
          </div>
          <button
            onClick={reconnect}
            className="px-4 py-1.5 text-sm border rounded-xl hover:bg-gray-50"
          >
            Reconnect
          </button>
          <button
            onClick={clearEvents}
            className="px-4 py-1.5 text-sm border rounded-xl hover:bg-gray-50"
          >
            Clear Events
          </button>
        </div>
      </div>

      {/* Live Events Panel */}
      {liveEvents.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Clock className="h-5 w-5" /> Live Events ({liveEvents.length})
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="max-h-80 overflow-y-auto space-y-3 pr-2">
              {liveEvents.slice(0, 8).map((event) => (
                <div
                  key={event.id}
                  className="flex gap-4 p-3 bg-gray-50 rounded-xl text-sm"
                >
                  <div className="font-mono text-xs text-gray-500 whitespace-nowrap pt-0.5">
                    {event.createdAt
                      ? format(parseISO(event.createdAt), "HH:mm:ss")
                      : "—"}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="font-medium truncate">
                      {event.stepName || event.eventType}
                    </div>
                    <div className="text-gray-600 text-sm">{event.message}</div>
                    {event.migrationId && (
                      <div className="text-xs text-blue-600 mt-1">
                        Migration: {event.migrationId}
                      </div>
                    )}
                  </div>
                  <Badge
                    variant={
                      event.eventType === "TASK_COMPLETED"
                        ? "success"
                        : "default"
                    }
                  >
                    {event.eventType}
                  </Badge>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Filters */}
      <div className="flex flex-col md:flex-row gap-4">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
          <input
            type="text"
            placeholder="Search by Doc ID or Title..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-2xl focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value as StatusFilter)}
          className="px-5 py-3 border border-gray-300 rounded-2xl focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="ALL">All Statuses</option>
          <option value="SUCCESS">Success</option>
          <option value="FAILED">Failed</option>
          <option value="RUNNING">Running</option>
          <option value="PENDING">Pending</option>
        </select>

        <select
          value={dateFilter}
          /* eslint-disable-next-line @typescript-eslint/no-explicit-any */
          onChange={(e) => setDateFilter(e.target.value as any)}
          className="px-5 py-3 border border-gray-300 rounded-2xl focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="all">All Time</option>
          <option value="24h">Last 24h</option>
          <option value="7d">Last 7 days</option>
          <option value="30d">Last 30 days</option>
        </select>
      </div>

      {/* Migrations Table */}
      <Card>
        <CardHeader>
          <CardTitle>
            Recent Migrations{/* no space */}
            <span className="text-sm font-normal text-gray-500 ml-2">
              ({filteredMigrations.length} shown)
            </span>
          </CardTitle>
        </CardHeader>
        <CardContent className="p-0">
          {filteredMigrations.length > 0 ? (
            <MigrationDataGrid data={filteredMigrations} />
          ) : (
            <div>{getMigrationStatus()}</div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
