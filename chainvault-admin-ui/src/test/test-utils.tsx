/*
 * Copyright (c) 2026. Gryphus Lab
 */
import React, { ReactElement, ReactNode, Suspense } from 'react'
import { render, type RenderOptions } from '@testing-library/react'
import { Provider } from 'react-redux'
import { BrowserRouter, MemoryRouter } from 'react-router-dom'
import { CSpinner } from '@coreui/react'
import store from '../store'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'

const AllTheProviders = ({ children }: { children: ReactNode }) => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        staleTime: Infinity,
      },
    },
  })
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>{children}</BrowserRouter>
    </QueryClientProvider>
  )
}

export const customRender = (ui: ReactElement) => {
  return render(ui, { wrapper: AllTheProviders })
}

/**
 * Resets the Redux store's UI-related state to a deterministic configuration used by tests.
 *
 * Sets `sidebarShow` to `true`, `sidebarUnfoldable` to `false`, and `theme` to `"light"`.
 */
export function resetStore() {
  store.dispatch({
    type: 'set',
    sidebarShow: true,
    sidebarUnfoldable: false,
    theme: 'light',
  })
}

function StoreOnly({ children }: Readonly<{ children: React.ReactNode }>) {
  return <Provider store={store}>{children}</Provider>
}

function StoreAndRouter({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <Provider store={store}>
      <MemoryRouter>{children}</MemoryRouter>
    </Provider>
  )
}

/** Redux only — use for `App`, which already wraps `HashRouter`. */
export function renderWithStore(ui: React.ReactElement, options?: Omit<RenderOptions, 'wrapper'>) {
  return render(ui, { wrapper: StoreOnly, ...options })
}

/** Redux + `MemoryRouter` — use for layouts and views that use `NavLink` / `Link`. */
export function renderWithProviders(
  ui: React.ReactElement,
  options?: Omit<RenderOptions, 'wrapper'>,
) {
  return render(ui, { wrapper: StoreAndRouter, ...options })
}

/** `App` + lazy routes: Redux, `Suspense`, no outer router. */
export function renderAppTree(ui: React.ReactElement, options?: Omit<RenderOptions, 'wrapper'>) {
  return render(ui, {
    wrapper: ({ children }) => (
      <StoreOnly>
        <Suspense
          fallback={
            <CSpinner color="primary" variant="grow" data-testid="route-suspense-fallback" />
          }
        >
          {children}
        </Suspense>
      </StoreOnly>
    ),
    ...options,
  })
}
