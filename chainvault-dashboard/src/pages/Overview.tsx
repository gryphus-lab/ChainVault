/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { useState, useMemo } from "react";
import { Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { format, parseISO, subDays } from "date-fns";
import { Search, X } from "lucide-react";

import { getMigrations, getMigrationStats } from "@/lib/api";
import type { Migration, MigrationStats } from "@/types";

import { Badge } from "@/components/ui/Badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/Card";
import { Skeleton, SkeletonCard } from "@/components/ui/Skeleton";

type StatusFilter = "ALL" | "SUCCESS" | "FAILED" | "RUNNING" | "PENDING";

export default function Overview() {
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState<StatusFilter>("ALL");
  const [dateFilter, setDateFilter] = useState<"all" | "24h" | "7d" | "30d">(
    "all",
  );

  const { data: stats, isLoading: statsLoading } = useQuery<MigrationStats>({
    queryKey: ["migration-stats"],
    queryFn: getMigrationStats,
    staleTime: 30 * 1000,
  });

  const { data: allMigrations = [], isLoading: migrationsLoading } = useQuery<
    Migration[]
  >({
    queryKey: ["migrations"],
    queryFn: () => getMigrations({ limit: 100 }),
    staleTime: 60 * 1000,
  });

  // Filtered and searched migrations
  const filteredMigrations = useMemo(() => {
    let result = [...allMigrations];

    // Status filter
    if (statusFilter !== "ALL") {
      result = result.filter((m) => m.status === statusFilter);
    }

    // Date filter
    if (dateFilter !== "all") {
      const days = dateFilter === "24h" ? 1 : dateFilter === "7d" ? 7 : 30;
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
        new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
    );
  }, [allMigrations, statusFilter, dateFilter, searchTerm]);

  if (statsLoading || migrationsLoading) {
    return (
      <div className="space-y-8">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          {Array.from({ length: 4 }).map((_, i) => (
            <SkeletonCard key={i} />
          ))}
        </div>
        <Card>
          <CardHeader>
            <Skeleton className="h-6 w-48" />
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {Array.from({ length: 8 }).map((_, i) => (
                <div
                  key={i}
                  className="h-16 bg-gray-100 rounded-lg animate-pulse"
                />
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      {/* Header + Filters */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <h1 className="text-3xl font-bold text-gray-900">
          Migration Dashboard
        </h1>

        <div className="flex flex-col sm:flex-row gap-3">
          {/* Search */}
          <div className="relative w-full md:w-80">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
            <input
              type="text"
              placeholder="Search by Doc ID or Title..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {/* Status Filter */}
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value as StatusFilter)}
            className="px-4 py-2.5 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="ALL">All Statuses</option>
            <option value="SUCCESS">Success</option>
            <option value="FAILED">Failed</option>
            <option value="RUNNING">Running</option>
            <option value="PENDING">Pending</option>
          </select>

          {/* Date Filter */}
          <select
            value={dateFilter}
            onChange={(e) =>
              setDateFilter(e.target.value as "all" | "24h" | "7d" | "30d")
            }
            className="px-4 py-2.5 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="all">All Time</option>
            <option value="24h">Last 24 hours</option>
            <option value="7d">Last 7 days</option>
            <option value="30d">Last 30 days</option>
          </select>

          {/* Clear Filters */}
          {(searchTerm || statusFilter !== "ALL" || dateFilter !== "all") && (
            <button
              onClick={() => {
                setSearchTerm("");
                setStatusFilter("ALL");
                setDateFilter("all");
              }}
              className="flex items-center gap-2 px-4 py-2.5 text-sm text-gray-600 hover:text-gray-900 border border-gray-300 rounded-xl hover:bg-gray-50 transition"
            >
              <X className="h-4 w-4" />
              Clear
            </button>
          )}
        </div>
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
            Recent Migrations
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
                        <Badge
                          variant={
                            migration.status === "SUCCESS"
                              ? "success"
                              : migration.status === "FAILED"
                                ? "danger"
                                : "default"
                          }
                        >
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
