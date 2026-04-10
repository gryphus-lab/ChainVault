/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { afterEach, vi } from 'vitest'
import React from 'react'
import '@testing-library/jest-dom/vitest'

vi.mock('@coreui/react-chartjs', () => {
  const make = (name: string) => {
    const Stub = React.forwardRef<HTMLDivElement, Record<string, unknown>>((props, ref) =>
      React.createElement('div', {
        ref,
        'data-testid': `chart-${name}`,
        ...props,
      }),
    )
    Stub.displayName = name
    return Stub
  }
  return {
    CChart: make('CChart'),
    CChartBar: make('CChartBar'),
    CChartBubble: make('CChartBubble'),
    CChartDoughnut: make('CChartDoughnut'),
    CChartLine: make('CChartLine'),
    CChartPie: make('CChartPie'),
    CChartPolarArea: make('CChartPolarArea'),
    CChartRadar: make('CChartRadar'),
    CChartScatter: make('CChartScatter'),
  }
})

const localStorageMock = (() => {
  let store: Record<string, string> = {}
  return {
    getItem: vi.fn((key: string) => store[key] ?? null),
    setItem: vi.fn((key: string, value: string) => {
      store[key] = value
    }),
    removeItem: vi.fn((key: string) => {
      delete store[key]
    }),
    clear: vi.fn(() => {
      store = {}
    }),
    get length() {
      return Object.keys(store).length
    },
    key: vi.fn((i: number) => Object.keys(store)[i] ?? null),
  }
})()

Object.defineProperty(globalThis, 'localStorage', {
  value: localStorageMock,
  writable: true,
})

class ResizeObserverMock {
  observe() {}
  unobserve() {}
  disconnect() {}
}
globalThis.ResizeObserver = ResizeObserverMock

window.scrollTo = vi.fn() as typeof window.scrollTo

const matchMediaFn = (query: string) => ({
  matches: false,
  media: query,
  onchange: null,
  addListener: () => {},
  removeListener: () => {},
  addEventListener: () => {},
  removeEventListener: () => {},
  dispatchEvent: () => false,
})

Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation(matchMediaFn),
})

const origGetComputedStyle = window.getComputedStyle.bind(window)
window.getComputedStyle = ((elt: Element, pseudoElt?: string | null) => {
  if (pseudoElt) {
    return origGetComputedStyle(document.documentElement) as CSSStyleDeclaration
  }
  if (!elt) {
    return origGetComputedStyle(document.documentElement)
  }
  try {
    return origGetComputedStyle(elt)
  } catch {
    return origGetComputedStyle(document.documentElement)
  }
}) as typeof window.getComputedStyle

class Canvas2DMock {
  canvas: HTMLCanvasElement
  constructor(canvas: HTMLCanvasElement) {
    this.canvas = canvas
  }
  fillRect = vi.fn()
  clearRect = vi.fn()
  getImageData = () => ({ data: new Uint8ClampedArray(), width: 0, height: 0 })
  putImageData = vi.fn()
  createImageData = () => new ImageData(1, 1)
  setTransform = vi.fn()
  drawImage = vi.fn()
  save = vi.fn()
  restore = vi.fn()
  beginPath = vi.fn()
  moveTo = vi.fn()
  lineTo = vi.fn()
  closePath = vi.fn()
  stroke = vi.fn()
  translate = vi.fn()
  scale = vi.fn()
  rotate = vi.fn()
  arc = vi.fn()
  fill = vi.fn()
  measureText = () => ({ width: 0 })
  transform = vi.fn()
  rect = vi.fn()
  clip = vi.fn()
}

HTMLCanvasElement.prototype.getContext = vi.fn(function (this: HTMLCanvasElement, type: string) {
  if (type === '2d') {
    return new Canvas2DMock(this) as unknown as CanvasRenderingContext2D
  }
  return null
}) as typeof HTMLCanvasElement.prototype.getContext

afterEach(() => {
  vi.clearAllMocks()
})
