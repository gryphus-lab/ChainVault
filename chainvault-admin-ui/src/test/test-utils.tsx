import React, { Suspense } from 'react'
import { render, type RenderOptions } from '@testing-library/react'
import { Provider } from 'react-redux'
import { MemoryRouter } from 'react-router-dom'
import { CSpinner } from '@coreui/react'
import store from 'src/store'

export function resetStore() {
  store.dispatch({
    type: 'set',
    sidebarShow: true,
    sidebarUnfoldable: false,
    theme: 'light',
  })
}

function StoreOnly({ children }: { children: React.ReactNode }) {
  return <Provider store={store}>{children}</Provider>
}

function StoreAndRouter({ children }: { children: React.ReactNode }) {
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
