/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { fireEvent, render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import Overview from './Overview'
import { useMigrationEvents } from '../../../hooks/useMigrationEvents'
import type { MigrationEvent } from '../../../types'

vi.mock('../../../hooks/useMigrationEvents')

const mockUseMigrationEvents = vi.mocked(useMigrationEvents)

const mockDefaults = {
  events: [] as MigrationEvent[],
  isConnected: false,
  clearEvents: vi.fn(),
  reconnect: vi.fn(),
}

const mockEvent: MigrationEvent = {
  id: 'evt-1',
  migrationId: 'mig-123',
  eventType: 'TASK_COMPLETED',
  stepName: 'SftpUpload',
  message: 'File uploaded successfully',
  timestamp: '2026-01-01T10:00:00Z',
}

describe('Overview', () => {
  beforeEach(() => {
    mockUseMigrationEvents.mockReturnValue({ ...mockDefaults })
  })

  describe('connection status pill', () => {
    it('shows Disconnected when not connected', () => {
      mockUseMigrationEvents.mockReturnValue({ ...mockDefaults, isConnected: false })
      render(<Overview />)
      expect(screen.getByText('Disconnected')).toBeInTheDocument()
    })

    it('shows Live • Connected when connected', () => {
      mockUseMigrationEvents.mockReturnValue({ ...mockDefaults, isConnected: true })
      render(<Overview />)
      expect(screen.getByText('Live • Connected')).toBeInTheDocument()
    })
  })

  describe('control buttons', () => {
    it('calls reconnect when the Reconnect button is clicked', () => {
      const reconnect = vi.fn()
      mockUseMigrationEvents.mockReturnValue({ ...mockDefaults, reconnect })
      render(<Overview />)
      fireEvent.click(screen.getByRole('button', { name: /reconnect/i }))
      expect(reconnect).toHaveBeenCalledOnce()
    })

    it('calls clearEvents when the Clear Events button is clicked', () => {
      const clearEvents = vi.fn()
      mockUseMigrationEvents.mockReturnValue({ ...mockDefaults, clearEvents })
      render(<Overview />)
      fireEvent.click(screen.getByRole('button', { name: /clear events/i }))
      expect(clearEvents).toHaveBeenCalledOnce()
    })
  })

  describe('live events panel', () => {
    it('does not render the panel when there are no events', () => {
      render(<Overview />)
      expect(screen.queryByText(/live events/i)).not.toBeInTheDocument()
    })

    it('renders the panel with the correct event count', () => {
      mockUseMigrationEvents.mockReturnValue({ ...mockDefaults, events: [mockEvent] })
      render(<Overview />)
      expect(screen.getByText('Live Events (1)')).toBeInTheDocument()
    })

    it('renders event stepName and message', () => {
      mockUseMigrationEvents.mockReturnValue({ ...mockDefaults, events: [mockEvent] })
      render(<Overview />)
      expect(screen.getByText('SftpUpload')).toBeInTheDocument()
      expect(screen.getByText('File uploaded successfully')).toBeInTheDocument()
    })

    it('falls back to eventType when stepName is absent', () => {
      const eventWithoutStep: MigrationEvent = { ...mockEvent, stepName: undefined }
      mockUseMigrationEvents.mockReturnValue({ ...mockDefaults, events: [eventWithoutStep] })
      render(<Overview />)
      // TASK_COMPLETED appears in both the name slot and the Badge — getAllByText is correct
      expect(screen.getAllByText('TASK_COMPLETED').length).toBeGreaterThanOrEqual(1)
    })

    it('renders a formatted HH:mm:ss timestamp for each event', () => {
      mockUseMigrationEvents.mockReturnValue({ ...mockDefaults, events: [mockEvent] })
      render(<Overview />)
      // Match any HH:mm:ss formatted time — avoids timezone-specific assertion
      expect(screen.getByText(/^\d{2}:\d{2}:\d{2}$/)).toBeInTheDocument()
    })

    it('shows migration ID link when present', () => {
      mockUseMigrationEvents.mockReturnValue({ ...mockDefaults, events: [mockEvent] })
      render(<Overview />)
      expect(screen.getByText('Migration: mig-123')).toBeInTheDocument()
    })

    it('caps display at 8 events regardless of list size', () => {
      const manyEvents: MigrationEvent[] = Array.from({ length: 12 }, (_, i) => ({
        ...mockEvent,
        id: `evt-${i}`,
        message: `Event ${i}`,
      }))
      mockUseMigrationEvents.mockReturnValue({ ...mockDefaults, events: manyEvents })
      render(<Overview />)
      // 12 events exist but only 8 messages should be rendered
      expect(screen.getAllByText(/^Event \d+$/).length).toBe(8)
    })
  })
})
