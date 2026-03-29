/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { useParams, Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { format, parseISO } from "date-fns";
import { ArrowLeft, FileText, Download } from "lucide-react";

import { getMigrationDetail } from "@/lib/api";
import type { MigrationDetail } from "@/types";

import Timeline from "@/components/Dashboard/Timeline";
import { Badge } from "@/components/ui/Badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/Card";

export default function MigrationDetailPage() {
  const { id } = useParams<{ id: string }>();

  const safeFormat = (
    dateStr: string | undefined | null,
    fallback: string = "—",
  ) => {
    if (!dateStr) return fallback;
    try {
      return format(parseISO(dateStr), "PPp");
    } catch {
      return fallback;
    }
  };

  const {
    data: migration,
    isLoading,
    error,
  } = useQuery<MigrationDetail>({
    queryKey: ["migration-detail", id],
    queryFn: () => getMigrationDetail(id!),
    enabled: !!id,
    staleTime: 30 * 1000,
  });

  if (isLoading) {
    return (
      <div className="text-center py-20">Loading migration details...</div>
    );
  }

  if (error || !migration) {
    return (
      <div className="text-center py-20">
        <p className="text-red-600">Failed to load migration {id}</p>
        <Link
          to="/"
          className="text-blue-600 hover:underline mt-4 inline-block"
        >
          ← Back to Dashboard
        </Link>
      </div>
    );
  }

  const statusClass =
    {
      SUCCESS: "bg-green-100 text-green-800",
      FAILED: "bg-red-100 text-red-800",
      RUNNING: "bg-blue-100 text-blue-800",
      PENDING: "bg-gray-100 text-gray-800",
    }[migration.status] || "bg-gray-100 text-gray-800";

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Link to="/" className="text-gray-500 hover:text-gray-900">
            <ArrowLeft className="h-6 w-6" />
          </Link>
          <div>
            <h1 className="text-3xl font-bold">Migration {migration.id}</h1>
            <p className="text-gray-600 mt-1">{migration.title}</p>
          </div>
        </div>
        <Badge className={statusClass}>{migration.status}</Badge>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm text-gray-500">Document ID</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="font-mono">{migration.docId}</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm text-gray-500">Created</CardTitle>
          </CardHeader>
          <CardContent>
            <p>{safeFormat(migration.createdAt)}</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm text-gray-500">Pages</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-3xl font-bold">{migration.pageCount}</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm text-gray-500">Trace ID</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="font-mono text-sm break-all">
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
          <Timeline events={migration.events} />
        </CardContent>
      </Card>

      {/* OCR Details */}
      <Card>
        <CardHeader>
          <CardTitle>OCR & Processing</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid md:grid-cols-2 gap-8 text-sm">
            <div>
              <p>
                <strong>OCR Attempted:</strong>{" "}
                {migration.ocrAttempted ? "Yes" : "No"}
              </p>
              <p>
                <strong>OCR Success:</strong>{" "}
                {migration.ocrSuccess ? "✅ Yes" : "❌ No"}
              </p>
              {migration.ocrPageCount && (
                <p>
                  <strong>Pages Processed:</strong> {migration.ocrPageCount}
                </p>
              )}
              {migration.ocrTotalTextLength && (
                <p>
                  <strong>Text Length:</strong>{" "}
                  {migration.ocrTotalTextLength.toLocaleString()} chars
                </p>
              )}
            </div>

            {migration.failureReason && (
              <div className="text-red-700 bg-red-50 p-4 rounded border border-red-100">
                <strong>Failure Reason:</strong> {migration.failureReason}
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Downloads */}
      {(migration.chainZipUrl || migration.pdfUrl) && (
        <Card>
          <CardHeader>
            <CardTitle>Downloads</CardTitle>
          </CardHeader>
          <CardContent className="flex gap-4">
            {migration.chainZipUrl && (
              <a
                href={migration.chainZipUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center gap-2 px-6 py-3 bg-gray-900 text-white rounded-xl hover:bg-gray-800"
              >
                <Download className="h-5 w-5" /> Chain ZIP
              </a>
            )}
            {migration.pdfUrl && (
              <a
                href={migration.pdfUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center gap-2 px-6 py-3 border border-gray-300 rounded-xl hover:bg-gray-50"
              >
                <FileText className="h-5 w-5" /> Merged PDF
              </a>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  );
}
