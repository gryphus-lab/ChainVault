/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { legacy_createStore as createStore } from 'redux'

export interface RootState {
  sidebarShow: boolean
  sidebarUnfoldable: boolean
  theme: string
}

/**
 * Initial state for the Redux store
 * @property {boolean} sidebarShow - Controls sidebar visibility (true = visible, false = hidden)
 * @property {string} theme - Current theme mode ('light', 'dark', or 'auto')
 */
const initialState: RootState = {
  sidebarShow: true,
  sidebarUnfoldable: false,
  theme: 'light',
}

/**
 * Root reducer function that handles all state changes
 *
 * @param {Object} state - Current state (defaults to initialState)
 * @param {Object} action - Action object with type and payload
 * @param {string} action.type - Action type ('set' to update state)
 * @returns {Object} New state object
 *
 * @example
 * // Update sidebar visibility
 * dispatch({ type: 'set', sidebarShow: false })
 *
 * @example
 * // Update theme
 * dispatch({ type: 'set', theme: 'dark' })
 *
 * @example
 * // Update multiple properties
 * dispatch({ type: 'set', sidebarShow: true, theme: 'light' })
 */
const changeState = (
  state: RootState | undefined,
  { type, ...rest }: { type: string } & Partial<RootState>,
): RootState => {
  const currentState = state ?? initialState
  return type === 'set' ? { ...currentState, ...rest } : currentState
}

/**
 * Redux store instance
 */
const store = createStore(changeState)
export type AppDispatch = typeof store.dispatch
export default store
