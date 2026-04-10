/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { useQuery } from '@tanstack/react-query'
import { format, parseISO } from 'date-fns'
import { Clock } from 'lucide-react'

import { getMigrations } from '../../../lib/api'
import { useMigrationEvents } from '../../../hooks/useMigrationEvents'

import { Badge } from '../../../components/Badge'
import { Card, CardContent, CardHeader, CardTitle } from '../../../components/Card'

/**
 * Render the Overview dashboard that displays live migration events and connection controls.
 *
 * The component shows connection status with reconnect/clear controls and a live events panel when events exist.
 *
 * @returns The React element tree for the Overview dashboard component
 */
export default function Overview() {

  useQuery({
    queryKey: ['migrations'],
    queryFn: async () => {
      const data = await getMigrations({ limit: 100 })
      return Array.isArray(data) ? data : []
    },
    retry: 2,
  })

  const { events: liveEvents, isConnected, clearEvents, reconnect } = useMigrationEvents()

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex justify-between">
        <div className="flex gap-3">
          <div
            className={`flex gap-2 px-4 py-1.5 rounded-full text-sm font-medium ${isConnected ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'}`}
          >
            <div
              className={`w-2.5 h-2.5 rounded-full ${isConnected ? 'bg-emerald-500 animate-pulse' : 'bg-red-500'}`}
            />
            {isConnected ? 'Live • Connected' : 'Disconnected'}
          </div>
          <button
            onClick={reconnect}
            className="px-4 py-1.5 text-sm border rounded-xl hover:bg-gray-50"
          >
            Reconnect
          </button>
          <button
            onClick={clearEvents}
            className="px-4 py-1.5 text-sm border rounded-xl hover:bg-gray-50"
          >
            Clear Events
          </button>
        </div>
      </div>

      {/* Live Events Panel */}
      {liveEvents.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Clock className="h-5 w-5" /> Live Events ({liveEvents.length})
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="max-h-80 overflow-y-auto space-y-3 pr-2">
              {liveEvents.slice(0, 8).map((event) => (
                <div key={event.id} className="flex gap-4 p-3 bg-gray-50 rounded-xl text-sm">
                  <div className="font-mono text-xs text-gray-500 whitespace-nowrap pt-0.5">
                    {event.timestamp ? format(parseISO(event.timestamp), 'HH:mm:ss') : '—'}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="font-medium truncate">{event.stepName || event.eventType}</div>
                    <div className="text-gray-600 text-sm">{event.message}</div>
                    {event.migrationId && (
                      <div className="text-xs text-blue-600 mt-1">
                        Migration: {event.migrationId}
                      </div>
                    )}
                  </div>
                  <Badge variant={event.eventType === 'TASK_COMPLETED' ? 'success' : 'default'}>
                    {event.eventType}
                  </Badge>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
}