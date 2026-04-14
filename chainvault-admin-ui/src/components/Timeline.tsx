/*
 * Copyright (c) 2026. Gryphus Lab
 */
import React from 'react'
import { differenceInSeconds, format, isValid, parseISO } from 'date-fns'
import { CheckCircle2, Clock, XCircle } from 'lucide-react'
import { CAlert, CBadge } from '@coreui/react'
import type { MigrationEvent } from '../types'

interface TimelineProps {
  events: MigrationEvent[]
}

const Timeline = ({ events }: TimelineProps) => {
  const sortedEvents = [...events]
    .filter((e) => e?.createdAt && isValid(parseISO(e.createdAt)))
    .map((e) => ({ ...e, parsedTime: parseISO(e.createdAt) }))
    .sort((a, b) => a.parsedTime.getTime() - b.parsedTime.getTime())

  return (
    <div className="timeline-wrapper pt-3" style={{ minHeight: '100px' }}>
      {sortedEvents.map((event, index) => {
        const isLast = index === sortedEvents.length - 1
        const prevTime = index > 0 ? sortedEvents[index - 1].parsedTime : null
        const currTime = event.parsedTime
        const duration = prevTime ? differenceInSeconds(currTime, prevTime) : 0

        return (
          <div key={event.id} className="position-relative mb-0">
            {/* Vertical Line Connector */}
            {!isLast && (
              <span
                className="position-absolute"
                style={{
                  left: '19px',
                  top: '40px',
                  bottom: '-10px',
                  width: '2px',
                  backgroundColor: '#ebedef', // CoreUI border color
                  zIndex: 0,
                }}
              />
            )}

            <div className="d-flex align-items-start pb-4 position-relative" style={{ zIndex: 1 }}>
              {/* Icon Section */}
              <div
                className="d-flex align-items-center justify-content-center bg-white rounded-circle border shadow-sm"
                style={{ width: '40px', height: '40px', flexShrink: 0 }}
              >
                {event.eventType === 'TASK_COMPLETED' && (
                  <CheckCircle2 size={20} className="text-success" />
                )}
                {event.eventType === 'TASK_FAILED' && <XCircle size={20} className="text-danger" />}
                {event.eventType === 'TASK_STARTED' && (
                  <Clock size={20} className="text-info opacity-75" />
                )}
              </div>

              {/* Content Section */}
              <div className="ms-3 flex-grow-1">
                <div className="d-flex justify-content-between align-items-center">
                  <h6>{event.taskType || event.eventType.replace(/_/g, ' ')}</h6>
                  <div className="text-end">
                    <small className="text-medium-emphasis fw-semibold">
                      {format(currTime, 'HH:mm:ss')}
                    </small>
                    {duration > 0 && (
                      <CBadge color="light" className="ms-2 text-dark border">
                        +{duration}s
                      </CBadge>
                    )}
                  </div>
                </div>

                <p className="text-medium-emphasis mb-1 mt-1" style={{ fontSize: '0.9rem' }}>
                  {event.message}
                </p>

                {event.errorMessage && (
                  <CAlert color="danger" className="py-2 px-3 mt-2 mb-2 small">
                    <strong>Error:</strong> {event.errorMessage}
                  </CAlert>
                )}

                {event.traceId && (
                  <div className="font-monospace text-muted mt-1" style={{ fontSize: '0.75rem' }}>
                    Trace ID: {event.traceId}
                  </div>
                )}
              </div>
            </div>
          </div>
        )
      })}
    </div>
  )
}

export default Timeline
