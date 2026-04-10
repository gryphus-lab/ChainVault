/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { format, parseISO } from 'date-fns'
import { Clock, Search } from 'lucide-react'

import { getMigrations } from '../../../lib/api'
import { useMigrationEvents } from '../../../hooks/useMigrationEvents'

import { Badge } from '../../../components/Badge'
import { Card, CardContent, CardHeader, CardTitle } from '../../../components/Card'

type StatusFilter = 'ALL' | 'SUCCESS' | 'FAILED' | 'RUNNING' | 'PENDING'

/**
 * Render the Overview dashboard that displays live migration events, connection controls, and filter inputs.
 *
 * The component shows connection status with reconnect/clear controls, a live events panel when events exist,
 * and UI controls for searching and filtering migrations by status and date range.
 *
 * @returns The React element tree for the Overview dashboard component
 */
export default function Overview() {
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL')
  const [dateFilter, setDateFilter] = useState<'all' | '24h' | '7d' | '30d'>('all')

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

      {/* Filters */}
      <div className="flex flex-col md:flex-row gap-4">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
          <input
            type="text"
            placeholder="Search by Doc ID or Title..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-2xl focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value as StatusFilter)}
          className="px-5 py-3 border border-gray-300 rounded-2xl focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="ALL">All Statuses</option>
          <option value="SUCCESS">Success</option>
          <option value="FAILED">Failed</option>
          <option value="RUNNING">Running</option>
          <option value="PENDING">Pending</option>
        </select>

        <select
          value={dateFilter}
          onChange={(e) => setDateFilter(e.target.value as 'all' | '24h' | '7d' | '30d')}
          className="px-5 py-3 border border-gray-300 rounded-2xl focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="all">All Time</option>
          <option value="24h">Last 24h</option>
          <option value="7d">Last 7 days</option>
          <option value="30d">Last 30 days</option>
        </select>
      </div>
    </div>
  )
}
