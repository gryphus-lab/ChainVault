import { useParams, Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { format, parseISO } from "date-fns";
import { ArrowLeft, FileText, Download, XCircle } from "lucide-react";

import { getMigrationDetail } from "../lib/api";
import type { MigrationDetail } from "../types";

import Timeline from "../components/Dashboard/Timeline";
import { Badge } from "../components/ui/Badge";
import { Card, CardContent, CardHeader, CardTitle } from "../components/ui/Card";
import { Skeleton, SkeletonText, SkeletonCard } from "../components/ui/Skeleton";

export default function MigrationDetailPage() {
  const { id } = useParams<{ id: string }>();

  const {
    data: migration,
    isLoading,
    error,
  } = useQuery<MigrationDetail>({
    queryKey: ["migration-detail", id],
    queryFn: () => getMigrationDetail(id!),
    enabled: !!id,
    retry: 2,
    staleTime: 60 * 1000,
  });

  // Loading State - Full page skeleton
  if (isLoading) {
    return (
      <div className="space-y-8">
        {/* Header Skeleton */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Skeleton className="h-8 w-8 rounded-full" /> {/* Back button */}
            <Skeleton className="h-9 w-96" /> {/* Title */}
          </div>
          <Skeleton className="h-8 w-24 rounded-full" /> {/* Status badge */}
        </div>

        {/* Stats Cards Skeleton */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          {Array.from({ length: 4 }).map((_, i) => (
            <SkeletonCard key={i} />
          ))}
        </div>

        {/* Timeline Skeleton */}
        <Card>
          <CardHeader>
            <Skeleton className="h-6 w-48" />
          </CardHeader>
          <CardContent className="space-y-8 py-6">
            {Array.from({ length: 6 }).map((_, i) => (
              <div key={i} className="flex gap-4">
                <Skeleton className="h-10 w-10 rounded-full" />
                <div className="flex-1 space-y-3">
                  <Skeleton className="h-4 w-3/4" />
                  <Skeleton className="h-4 w-1/2" />
                </div>
                <Skeleton className="h-4 w-20" />
              </div>
            ))}
          </CardContent>
        </Card>

        {/* OCR Details Skeleton */}
        <Card>
          <CardHeader>
            <Skeleton className="h-6 w-64" />
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
              <div className="space-y-4">
                <Skeleton className="h-5 w-40" />
                <SkeletonText lines={4} />
              </div>
              <div className="space-y-4">
                <Skeleton className="h-5 w-40" />
                <SkeletonText lines={3} />
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  // Error State
  if (error || !migration) {
    return (
      <div className="flex flex-col items-center justify-center py-24 text-center">
        <div className="text-red-500 mb-6">
          <XCircle className="h-20 w-20 mx-auto" />
        </div>
        <h2 className="text-2xl font-semibold text-gray-900 mb-3">
          Failed to load migration
        </h2>
        <p className="text-gray-600 mb-8 max-w-md">
          Could not retrieve details for migration{" "}
          <span className="font-mono">#{id}</span>.
        </p>
        <Link
          to="/"
          className="inline-flex items-center px-6 py-3 bg-gray-900 text-white rounded-xl hover:bg-gray-800 transition-colors"
        >
          ← Back to Dashboard
        </Link>
      </div>
    );
  }

  // Status styles
  const statusStyles =
    {
      SUCCESS: "bg-green-100 text-green-800 border-green-200",
      FAILED: "bg-red-100 text-red-800 border-red-200",
      COMPENSATED: "bg-yellow-100 text-yellow-800 border-yellow-200",
      RUNNING: "bg-blue-100 text-blue-800 border-blue-200",
      PENDING: "bg-gray-100 text-gray-800 border-gray-200",
    }[migration.status] || "bg-gray-100 text-gray-800";

  return (
    <div className="space-y-8 pb-12">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Link
            to="/"
            className="text-gray-500 hover:text-gray-900 transition-colors"
          >
            <ArrowLeft className="h-6 w-6" />
          </Link>
          <div>
            <h1 className="text-3xl font-bold text-gray-900">
              Migration {migration.id}
            </h1>
            <p className="text-gray-600 mt-1">{migration.title}</p>
          </div>
        </div>

        <Badge
          className={`px-5 py-1.5 text-sm font-medium border ${statusStyles}`}
        >
          {migration.status}
        </Badge>
      </div>

      {/* Key Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">
              Document ID
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="font-mono text-lg tracking-tight">
              {migration.docId}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">
              Created At
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-lg font-medium">
              {format(parseISO(migration.createdAt), "PPP")}
            </p>
            <p className="text-sm text-gray-500">
              {format(parseISO(migration.createdAt), "p")}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">
              Pages
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-3xl font-bold">{migration.pageCount}</p>
            <p className="text-sm text-gray-500 mt-1">
              OCR:{" "}
              {migration.ocrAttempted
                ? migration.ocrSuccess
                  ? "✅ Success"
                  : "❌ Failed"
                : "Not attempted"}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">
              Trace ID
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="font-mono text-sm break-all bg-gray-50 p-3 rounded border">
              {migration.traceId || "—"}
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Timeline */}
      <Card>
        <CardHeader>
          <CardTitle>Migration Timeline</CardTitle>
        </CardHeader>
        <CardContent>
          <Timeline events={migration.events || []} />
        </CardContent>
      </Card>

      {/* OCR & Processing Details */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <FileText className="h-5 w-5" />
            OCR & Processing Details
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
            <div>
              <h4 className="font-medium text-gray-700 mb-3">OCR Summary</h4>
              <div className="space-y-3 text-sm">
                <div className="flex justify-between">
                  <span className="text-gray-500">Attempted</span>
                  <span>{migration.ocrAttempted ? "Yes" : "No"}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Success</span>
                  <span>
                    {migration.ocrSuccess === true
                      ? "✅ Yes"
                      : migration.ocrSuccess === false
                        ? "❌ No"
                        : "—"}
                  </span>
                </div>
                {migration.ocrPageCount && (
                  <div className="flex justify-between">
                    <span className="text-gray-500">Pages Processed</span>
                    <span>{migration.ocrPageCount}</span>
                  </div>
                )}
                {migration.ocrTotalTextLength && (
                  <div className="flex justify-between">
                    <span className="text-gray-500">Extracted Text</span>
                    <span>
                      {migration.ocrTotalTextLength.toLocaleString()} characters
                    </span>
                  </div>
                )}
              </div>
            </div>

            {migration.failureReason && (
              <div>
                <h4 className="font-medium text-red-600 mb-3">
                  Failure Reason
                </h4>
                <div className="bg-red-50 border border-red-100 p-4 rounded-lg text-red-700">
                  {migration.failureReason}
                </div>
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Download Section */}
      {(migration.chainZipUrl || migration.pdfUrl) && (
        <Card>
          <CardHeader>
            <CardTitle>Downloads</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-wrap gap-4">
              {migration.chainZipUrl && (
                <a
                  href={migration.chainZipUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-2 px-6 py-3 bg-gray-900 hover:bg-gray-800 text-white rounded-xl transition"
                >
                  <Download className="h-5 w-5" />
                  Download Chain ZIP
                </a>
              )}

              {migration.pdfUrl && (
                <a
                  href={migration.pdfUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-2 px-6 py-3 border border-gray-300 hover:bg-gray-50 rounded-xl transition"
                >
                  <FileText className="h-5 w-5" />
                  Download Merged PDF
                </a>
              )}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
