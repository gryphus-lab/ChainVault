/**
 * Redux Store Configuration
 *
 * Simple Redux store managing global application state.
 * Handles sidebar visibility and theme preferences.
 *
 * @module store
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
 * @param {...*} rest - Additional properties to merge into state
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
  state: RootState = initialState,
  { type, ...rest }: { type: string } & Partial<RootState>,
): RootState => {
  if (type === 'set') {
    return { ...state, ...rest }
  } else {
    return state
  }
}

/**
 * Redux store instance
 */
const store = createStore(changeState)
export type AppDispatch = typeof store.dispatch
export default store
