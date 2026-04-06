import { describe, it, expect, beforeEach } from 'vitest'
import store from './store'

describe('Redux store', () => {
  beforeEach(() => {
    store.dispatch({
      type: 'set',
      sidebarShow: true,
      sidebarUnfoldable: false,
      theme: 'light',
    })
  })

  it('exposes initial sidebar and theme state', () => {
    const state = store.getState()
    expect(state.sidebarShow).toBe(true)
    expect(state.sidebarUnfoldable).toBe(false)
    expect(state.theme).toBe('light')
  })

  it('merges properties on set action', () => {
    store.dispatch({ type: 'set', sidebarShow: false, theme: 'dark' })
    const state = store.getState()
    expect(state.sidebarShow).toBe(false)
    expect(state.theme).toBe('dark')
  })
})
