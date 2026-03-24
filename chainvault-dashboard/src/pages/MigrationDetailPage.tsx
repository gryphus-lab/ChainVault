/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { useParams, Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { format, parseISO } from "date-fns";
import { ArrowLeft, FileText, Download } from "lucide-react";

import { getMigrationDetail } from "../lib/api";
import type { MigrationDetail } from "../types";

import Timeline from "../components/Dashboard/Timeline";
import { Badge } from "../components/ui/Badge";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "../components/ui/Card";
import { Skeleton } from "../components/ui/Skeleton";

const getColors = (migration: MigrationDetail) => {
  if (migration.ocrSuccess) {
    if (migration.ocrAttempted) {
      return <>
        {"✅ Success"}
      </>;
    } else {
      return <>
        {"Not attempted"}
      </>;
    }
  } else {
      return <>
        {"❌ Failed"}
      </>;
    }
};

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
    staleTime: 60 * 1000, // 1 minute
  });

  // Loading State
  if (isLoading) {
    return (
      <div className="space-y-8">
        <div className="flex items-center gap-4">
          <Link to="/" className="text-gray-500 hover:text-gray-700">
            <ArrowLeft className="h-6 w-6" />
          </Link>
          <Skeleton className="h-8 w-64" />
        </div>

        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          {[...new Array(4)].map((_, i) => (
            <Skeleton key={i} className="h-32 rounded-lg" />
          ))}
        </div>

        <Card>
          <CardHeader>
            <Skeleton className="h-6 w-48" />
          </CardHeader>
          <CardContent className="space-y-6">
            {[...new Array(5)].map((_, i) => (
              <div key={i} className="flex gap-4">
                <Skeleton className="h-10 w-10 rounded-full" />
                <div className="flex-1 space-y-2">
                  <Skeleton className="h-4 w-3/4" />
                  <Skeleton className="h-4 w-1/2" />
                </div>
              </div>
            ))}
          </CardContent>
        </Card>
      </div>
    );
  }

  // Error State
  if (error || !migration) {
    return (
      <div className="flex flex-col items-center justify-center py-20 text-center">
        <div className="text-red-500 mb-4">
          <XCircle className="h-16 w-16 mx-auto" />
        </div>
        <h2 className="text-2xl font-semibold text-gray-900 mb-2">
          Failed to load migration
        </h2>
        <p className="text-gray-600 mb-6">
          Could not retrieve details for migration ID: {id}
        </p>
        <Link
          to="/"
          className="inline-flex items-center px-4 py-2 bg-gray-900 text-white rounded-lg hover:bg-gray-800 transition"
        >
          ← Back to Dashboard
        </Link>
      </div>
    );
  }

  // Status styling
  const statusStyles = {
    SUCCESS: "bg-green-100 text-green-800 border-green-200",
    FAILED: "bg-red-100 text-red-800 border-red-200",
    RUNNING: "bg-blue-100 text-blue-800 border-blue-200",
    PENDING: "bg-gray-100 text-gray-800 border-gray-200",
  };

  return (
    <div className="space-y-8 pb-12">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Link to="/" className="text-gray-500 hover:text-gray-900 transition">
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
          className={`px-4 py-1 text-sm font-medium border ${statusStyles[migration.status]}`}
        >
          {migration.status}
        </Badge>
      </div>

      {/* Key Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">
              Document ID
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="font-mono text-lg">{migration.docId}</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-gray-500">
              Created
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
              {getColors(migration)}
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
            <p className="font-mono text-sm break-all bg-gray-100 p-2 rounded">
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
          <Timeline events={migration.events || []} isLoading={false} />
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
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h4 className="font-medium text-gray-700 mb-2">OCR Summary</h4>
              <div className="space-y-2 text-sm">
                <p>
                  <span className="text-gray-500">Attempted:</span>{" "}
                  {migration.ocrAttempted ? "Yes" : "No"}
                </p>
                <p>
                  <span className="text-gray-500">Success:</span>
                  {migration.ocrSuccess === true
                    ? "✅ Yes"
                    : migration.ocrSuccess === false
                      ? "❌ No"
                      : "—"}
                </p>
                {migration.ocrPageCount && (
                  <p>
                    <span className="text-gray-500">Pages Processed:</span>{" "}
                    {migration.ocrPageCount}
                  </p>
                )}
                {migration.ocrTotalTextLength && (
                  <p>
                    <span className="text-gray-500">Extracted Text:</span>{" "}
                    {migration.ocrTotalTextLength.toLocaleString()} characters
                  </p>
                )}
              </div>
            </div>

            {migration.failureReason && (
              <div>
                <h4 className="font-medium text-red-600 mb-2">
                  Failure Reason
                </h4>
                <p className="text-red-700 bg-red-50 p-3 rounded border border-red-100">
                  {migration.failureReason}
                </p>
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Download Links */}
      {(migration.chainZipUrl || migration.pdfUrl) && (
        <Card>
          <CardHeader>
            <CardTitle>Downloads</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex gap-4">
              {migration.chainZipUrl && (
                <a
                  href={migration.chainZipUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-2 px-5 py-3 bg-gray-900 text-white rounded-lg hover:bg-gray-800 transition"
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
                  className="inline-flex items-center gap-2 px-5 py-3 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition"
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
