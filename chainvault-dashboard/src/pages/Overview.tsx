import { Link } from "react-router-dom";
import { useMigrationStats, useMigrations } from "../hooks/useMigration";
import { format } from "date-fns";
import { AlertCircle, CheckCircle, Clock, XCircle } from "lucide-react";
import type { Migration } from "../types";

function getBgTextColors(m: Migration) {
  if (m.status === "FAILED") {
    return "bg-red-100 text-red-800";
  } else if (m.status === "SUCCESS") {
    return "bg-green-100 text-green-800";
  } else {
    return "bg-blue-100 text-blue-800";
  }
}

export function Overview() {
  const { data: stats, isLoading: statsLoading } = useMigrationStats();
  const { data: migrations = [], isLoading: migrationsLoading } =
    useMigrations();

  if (statsLoading || migrationsLoading) {
    return <div className="text-center py-12">Loading...</div>;
  }

  return (
    <div className="space-y-8">
      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div className="bg-white p-6 rounded-lg shadow">
          <div className="flex items-center">
            <Clock className="h-8 w-8 text-blue-500" />
            <div className="ml-4">
              <p className="text-sm text-gray-500">Pending / Running</p>
              <p className="text-2xl font-bold">
                {stats?.pending ?? 0 + stats?.running ?? 0}
              </p>
            </div>
          </div>
        </div>

        <div className="bg-white p-6 rounded-lg shadow">
          <div className="flex items-center">
            <CheckCircle className="h-8 w-8 text-green-500" />
            <div className="ml-4">
              <p className="text-sm text-gray-500">Success</p>
              <p className="text-2xl font-bold">{stats?.success ?? 0}</p>
            </div>
          </div>
        </div>

        <div className="bg-white p-6 rounded-lg shadow">
          <div className="flex items-center">
            <XCircle className="h-8 w-8 text-red-500" />
            <div className="ml-4">
              <p className="text-sm text-gray-500">Failed</p>
              <p className="text-2xl font-bold">{stats?.failed ?? 0}</p>
            </div>
          </div>
        </div>

        <div className="bg-white p-6 rounded-lg shadow">
          <div className="flex items-center">
            <AlertCircle className="h-8 w-8 text-yellow-500" />
            <div className="ml-4">
              <p className="text-sm text-gray-500">Compensated</p>
              <p className="text-2xl font-bold">{stats?.compensated ?? 0}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Recent Migrations Table */}
      <div className="bg-white shadow rounded-lg">
        <div className="px-6 py-5 border-b border-gray-200">
          <h3 className="text-lg font-medium text-gray-900">
            Recent Migrations
          </h3>
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  ID
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  Title
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  Created
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  Updated
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {migrations.slice(0, 10).map((m) => (
                <tr key={m.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    <Link
                      to={`/migration/${m.id}`}
                      className="text-blue-600 hover:underline"
                    >
                      {m.id}
                    </Link>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {m.title}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span
                      className={`px-2 py-1 text-xs rounded-full ${getBgTextColors(
                        m,
                      )}`}
                    >
                      {m.status}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {format(new Date(m.createdAt), "PPp")}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {format(new Date(m.updatedAt), "PPp")}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
