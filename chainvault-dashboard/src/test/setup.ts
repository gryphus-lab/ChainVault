/*
 * Copyright (c) 2026. Gryphus Lab
 */
import '@testing-library/jest-dom'
import { afterEach, vi } from 'vitest'
import { cleanup } from '@testing-library/react'

// Cleanup after each test
afterEach(() => {
  cleanup()
  MockEventSource.instances.length = 0
})

/* eslint-disable @typescript-eslint/no-explicit-any */
class MockEventSource {
  static readonly instances: MockEventSource[] = []

  url: string
  readyState: number = 0

  onopen: ((ev: Event) => any) | null = null
  onmessage: ((ev: MessageEvent) => any) | null = null
  onerror: ((ev: Event) => any) | null = null

  close = vi.fn()

  constructor(url: string) {
    this.url = url
    MockEventSource.instances.push(this)
  }

  // --- helpers for tests ---
  emitOpen() {
    this.readyState = 1
    this.onopen?.(new Event('open'))
  }

  emitMessage(data: any) {
    this.onmessage?.(
      new MessageEvent('message', {
        data: typeof data === 'string' ? data : JSON.stringify(data),
      }),
    )
  }

  emitError() {
    this.readyState = 2
    this.onerror?.(new Event('error'))
  }
}

// attach globally
;(globalThis as any).EventSource = MockEventSource

// expose for tests
;(globalThis as any).__MockEventSource__ = MockEventSource
