/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { useState, useMemo } from "react";
import { Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { format, parseISO, subDays } from "date-fns";
import { Search, RefreshCw } from "lucide-react";

import { getMigrations, getMigrationStats } from "@/lib/api";
import { useMigrationEvents } from "@/hooks/useMigration";

import { Badge } from "@/components/ui/Badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/Card";
import { Migration } from "@/types";

type StatusFilter = "ALL" | "SUCCESS" | "FAILED" | "RUNNING" | "PENDING";

function getVariant(migration: Migration) {
  switch (migration.status) {
    case "SUCCESS":
      return "success";
    case "FAILED":
      return "danger";
    default:
      return "default";
  }
}

export default function Overview() {
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState<StatusFilter>("ALL");
  const [dateFilter, setDateFilter] = useState<"all" | "24h" | "7d" | "30d">(
    "all",
  );

  const { data: stats } = useQuery({
    queryKey: ["migration-stats"],
    queryFn: getMigrationStats,
  });

  const { data: allMigrations = [] } = useQuery({
    queryKey: ["migrations"],
    queryFn: () => getMigrations({ limit: 100 }),
  });

  // Real-time events
  const { events: liveEvents, isConnected, clearEvents } = useMigrationEvents();

  // Merge live events into migrations (for real-time status updates)
  const migrationsWithLive = useMemo(() => {
    const merged = [...allMigrations];

    liveEvents.forEach((liveEvent) => {
      const index = merged.findIndex((m) => m.id === liveEvent.migrationId);
      if (index !== -1) {
        merged[index] = {
          ...merged[index],
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          status: liveEvent.status as any,
          updatedAt: liveEvent.timestamp,
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
      result = result.filter((m) => new Date(m.createdAt) >= cutoff);
    }

    // Search term (docId or title)
    if (searchTerm.trim()) {
      const term = searchTerm.toLowerCase().trim();
      result = result.filter(
        (m) =>
          m.docId.toLowerCase().includes(term) ||
          m.title.toLowerCase().includes(term),
      );
    }

    // Sort by most recent first
    return result.sort(
      (a, b) =>
        new Date(b.updatedAt || b.createdAt).getTime() -
        new Date(a.updatedAt || a.createdAt).getTime(),
    );
  }, [migrationsWithLive, statusFilter, dateFilter, searchTerm]);

  return (
    <div className="space-y-8">
      {/* Header with Connection Status */}
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-gray-900">
          Migration Dashboard
        </h1>

        <div className="flex items-center gap-3">
          <div
            className={`flex items-center gap-2 px-3 py-1 rounded-full text-sm ${isConnected ? "bg-green-100 text-green-700" : "bg-red-100 text-red-700"}`}
          >
            <div
              className={`w-2 h-2 rounded-full ${isConnected ? "bg-green-500 animate-pulse" : "bg-red-500"}`}
            />
            {isConnected ? "Live" : "Disconnected"}
          </div>

          <button
            onClick={clearEvents}
            className="text-sm text-gray-500 hover:text-gray-700 flex items-center gap-1"
          >
            <RefreshCw className="h-4 w-4" />
            Clear Live
          </button>
        </div>
      </div>

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

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">
              Total Migrations
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-4xl font-bold text-gray-900">
              {stats?.total ?? 0}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">
              Successful
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-4xl font-bold text-green-600">
              {stats?.success ?? 0}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">
              Failed
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-4xl font-bold text-red-600">
              {stats?.failed ?? 0}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">
              In Progress
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-4xl font-bold text-blue-600">
              {(stats?.pending ?? 0) + (stats?.running ?? 0)}
            </p>
          </CardContent>
        </Card>
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
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-4 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Migration ID
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Title
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Status
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Created
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Updated
                  </th>
                  <th className="px-6 py-4 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredMigrations.length > 0 ? (
                  filteredMigrations.map((migration) => (
                    <tr
                      key={migration.id}
                      className="hover:bg-gray-50 transition"
                    >
                      <td className="px-6 py-5 whitespace-nowrap font-mono text-sm text-gray-900">
                        {migration.id}
                      </td>
                      <td className="px-6 py-5 whitespace-nowrap text-sm text-gray-700 max-w-xs truncate">
                        {migration.title}
                      </td>
                      <td className="px-6 py-5 whitespace-nowrap">
                        <Badge variant={getVariant(migration)}>
                          {migration.status}
                        </Badge>
                      </td>
                      <td className="px-6 py-5 whitespace-nowrap text-sm text-gray-500">
                        {format(parseISO(migration.createdAt), "PPp")}
                      </td>
                      <td className="px-6 py-5 whitespace-nowrap text-sm text-gray-500">
                        {format(parseISO(migration.updatedAt), "PPp")}
                      </td>
                      <td className="px-6 py-5 whitespace-nowrap text-right">
                        <Link
                          to={`/migration/${migration.id}`}
                          className="text-blue-600 hover:text-blue-700 font-medium text-sm"
                        >
                          View Details →
                        </Link>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td
                      colSpan={6}
                      className="px-6 py-12 text-center text-gray-500"
                    >
                      No migrations found matching your filters.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
