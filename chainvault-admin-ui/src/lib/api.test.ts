/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  api,
  getMigrations,
  getMigrationStats,
  getMigrationDetail,
  getMigrationEvents,
} from './api'

describe('API Service', () => {
  // Mock the .json() resolution
  const mockJsonResponse = (data: any) => ({
    json: vi.fn().mockResolvedValue(data),
  })

  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('getMigrations calls the correct endpoint with params', async () => {
    const mockData = [{ id: '1' }]
    const spy = vi.spyOn(api, 'get').mockReturnValue(mockJsonResponse(mockData) as any)

    const result = await getMigrations({ limit: 10 })

    expect(spy).toHaveBeenCalledWith('migrations', {
      searchParams: { limit: 10 },
    })
    expect(result).toEqual(mockData)
  })

  it('getMigrationStats calls the stats endpoint', async () => {
    const mockStats = { total: 5 }
    const spy = vi.spyOn(api, 'get').mockReturnValue(mockJsonResponse(mockStats) as any)

    const result = await getMigrationStats()

    expect(spy).toHaveBeenCalledWith('migrations/stats')
    expect(result).toEqual(mockStats)
  })

  it('getMigrationDetail calls the specific ID endpoint', async () => {
    const mockDetail = { id: '123', status: 'completed' }
    const spy = vi.spyOn(api, 'get').mockReturnValue(mockJsonResponse(mockDetail) as any)

    const result = await getMigrationDetail('123')

    expect(spy).toHaveBeenCalledWith('migrations/123/detail')
    expect(result).toEqual(mockDetail)
  })

  it('getMigrationEvents calls the events sub-resource', async () => {
    const mockEvents = [{ event: 'started' }]
    const spy = vi.spyOn(api, 'get').mockReturnValue(mockJsonResponse(mockEvents) as any)

    const result = await getMigrationEvents('abc')

    expect(spy).toHaveBeenCalledWith('migrations/abc/events')
    expect(result).toEqual(mockEvents)
  })
})
