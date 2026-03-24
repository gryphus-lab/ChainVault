/*
 * Copyright (c) 2026. Gryphus Lab
 */
export interface Migration {
  id: string;
  docId: string;
  title: string;
  status: "PENDING" | "RUNNING" | "SUCCESS" | "FAILED";
  createdAt: string;
  updatedAt: string;
  processInstanceKey: string;
  traceId?: string;
  pageCount: number;
  ocrAttempted: boolean;
  ocrSuccess?: boolean;
  ocrPageCount?: number;
  ocrTotalTextLength?: number;
  failureReason?: string;
}

export interface MigrationStats {
  total: number;
  pending: number;
  running: number;
  success: number;
  failed: number;
  compensated: number;
  last24h: number;
}

export interface MigrationEvent {
  id: string;
  migrationId: string;
  eventType: "TASK_STARTED" | "TASK_COMPLETED" | "TASK_FAILED";
  stepName?: string;
  message: string;
  timestamp: string;
  traceId?: string;
  status: "SUCCESS" | "FAILED" | "RUNNING" | "PENDING";
  durationMs?: number;
  errorCode?: string;
  errorMessage?: string;
}

export interface MigrationDetail extends Migration {
  events: MigrationEvent[];
  ocrTextPreview?: string;
  chainZipUrl?: string;
  pdfUrl?: string;
}
