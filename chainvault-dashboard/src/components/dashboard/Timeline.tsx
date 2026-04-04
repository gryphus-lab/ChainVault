/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { format, parseISO, differenceInSeconds } from "date-fns";
import { CheckCircle2, XCircle, Clock } from "lucide-react";
import type { MigrationEvent } from "@/types";

interface TimelineProps {
  events: MigrationEvent[];
}

const Timeline = ({ events }: TimelineProps) => {
  if (events.length === 0) {
    return (
      <div className="py-12 text-center text-gray-500">
        No timeline events available yet.
      </div>
    );
  }

  const sortedEvents = [...events]
    .filter((e) => e?.timestamp)
    .sort(
      (a, b) =>
        new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime(),
    );

  return (
    <div className="flow-root">
      <ul className="-mb-8">
        {sortedEvents.map((event, index) => {
          const isLast = index === sortedEvents.length - 1;
          const prevTime =
            index > 0 ? parseISO(sortedEvents[index - 1].timestamp) : null;
          const currTime = parseISO(event.timestamp);
          const duration = prevTime
            ? differenceInSeconds(currTime, prevTime)
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
                  {/* Icon */}
                  <div className="flex h-10 w-10 items-center justify-center rounded-full bg-white ring-8 ring-white">
                    {event.eventType === "TASK_COMPLETED" && (
                      <CheckCircle2 className="h-6 w-6 text-green-600" />
                    )}
                    {event.eventType === "TASK_FAILED" && (
                      <XCircle className="h-6 w-6 text-red-600" />
                    )}
                    {event.eventType === "TASK_STARTED" && (
                      <Clock className="h-6 w-6 text-blue-600 animate-pulse" />
                    )}
                  </div>

                  <div className="min-w-0 flex-1 pt-1.5">
                    <div className="flex justify-between items-start">
                      <div>
                        <p className="font-semibold text-gray-900">
                          {event.stepName || event.eventType.replace("_", " ")}
                        </p>
                        <p className="text-sm text-gray-600 mt-0.5">
                          {event.message}
                        </p>
                      </div>

                      <div className="text-right text-sm text-gray-500 whitespace-nowrap">
                        {format(currTime, "HH:mm:ss")}
                        {duration > 0 && (
                          <span className="ml-2 text-xs text-gray-400">
                            +{duration}s
                          </span>
                        )}
                      </div>
                    </div>

                    {event.errorMessage && (
                      <div className="mt-3 text-sm bg-red-50 border border-red-100 p-3 rounded text-red-700">
                        {event.errorMessage}
                      </div>
                    )}

                    {event.traceId && (
                      <p className="mt-2 text-xs font-mono text-gray-500">
                        Trace ID: {event.traceId}
                      </p>
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
};

export default Timeline;
