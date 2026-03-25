/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { format, parseISO, differenceInMilliseconds } from "date-fns";
import { CheckCircle2, XCircle, Clock, AlertTriangle } from "lucide-react";
import type { MigrationEvent } from "@/types";

interface TimelineProps {
  events: MigrationEvent[];
  isLoading?: boolean;
}

export default function Timeline({
  events,
  isLoading = false,
}: Readonly<TimelineProps>) {
  if (isLoading) {
    return (
      <div className="py-8 text-center text-gray-500">Loading timeline...</div>
    );
  }

  if (events.length === 0) {
    return (
      <div className="py-8 text-center text-gray-500">
        No events recorded yet.
      </div>
    );
  }

  // Sort events by timestamp (just in case backend doesn't)
  const sortedEvents = [...events].sort(
    (a, b) => parseISO(a.timestamp).getTime() - parseISO(b.timestamp).getTime(),
  );

  return (
    <div className="flow-root">
      <ul className="-mb-8">
        {sortedEvents.map((event, idx) => {
          const isLast = idx === sortedEvents.length - 1;
          const prevTime =
            idx > 0 ? parseISO(sortedEvents[idx - 1].timestamp) : null;
          const currentTime = parseISO(event.timestamp);
          const duration = prevTime
            ? differenceInMilliseconds(currentTime, prevTime)
            : 0;

          return (
            <li key={event.id}>
              <div className="relative pb-8">
                {!isLast && (
                  <span
                    className="absolute left-5 top-8 -ml-px h-full w-0.5 bg-gray-200"
                    aria-hidden="true"
                  />
                )}

                <div className="relative flex items-start space-x-4">
                  {/* Status Icon */}
                  <div className="flex h-10 w-10 items-center justify-center rounded-full ring-8 ring-white bg-white">
                    {event.status === "SUCCESS" && (
                      <CheckCircle2 className="h-6 w-6 text-green-600" />
                    )}
                    {event.status === "FAILED" && (
                      <XCircle className="h-6 w-6 text-red-600" />
                    )}
                    {event.status === "RUNNING" && (
                      <Clock className="h-6 w-6 text-blue-600 animate-pulse" />
                    )}
                    {event.status === "PENDING" && (
                      <AlertTriangle className="h-6 w-6 text-amber-600" />
                    )}
                  </div>

                  <div className="min-w-0 flex-1 pt-1">
                    <div className="flex justify-between">
                      <div>
                        <p className="text-sm font-semibold text-gray-900">
                          {event.stepName || event.eventType}
                        </p>
                        <p className="mt-0.5 text-sm text-gray-600">
                          {event.message}
                        </p>
                      </div>

                      <div className="text-right text-sm text-gray-500 whitespace-nowrap">
                        {format(currentTime, "HH:mm:ss")}
                        {duration > 0 && (
                          <span className="ml-2 text-xs text-gray-400">
                            (+{duration}ms)
                          </span>
                        )}
                      </div>
                    </div>

                    {event.errorMessage && (
                      <div className="mt-2 text-sm text-red-600 bg-red-50 p-2 rounded border border-red-100">
                        {event.errorMessage}
                      </div>
                    )}

                    {event.traceId && (
                      <div className="mt-1 text-xs text-gray-500 font-mono">
                        Trace: {event.traceId}
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </li>
          );
        })}
      </ul>
    </div>
  );
}
