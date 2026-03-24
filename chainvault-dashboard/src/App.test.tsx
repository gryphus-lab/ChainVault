/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';   // ← Use MemoryRouter in tests
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import App from './App';

// Mock API
vi.mock('@/lib/api', () => ({
  getMigrationStats: vi.fn().mockResolvedValue({
    total: 42,
    pending: 5,
    running: 3,
    success: 28,
    failed: 4,
    compensated: 2,
    last24h: 12,
  }),
  getMigrations: vi.fn().mockResolvedValue([ /* your mock data */ ]),
}));

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: false, staleTime: Infinity } },
});

const renderWithProviders = (ui: React.ReactElement) => {
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>   {/* ← Use MemoryRouter instead of BrowserRouter in tests */}
        {ui}
      </MemoryRouter>
    </QueryClientProvider>
  );
};

describe('App Component', () => {
  it('renders dashboard title', () => {
    renderWithProviders(<App />);
    expect(screen.getByText(/ChainVault Migration Dashboard/i)).toBeInTheDocument();
  });

  it('renders stats cards', async () => {
    renderWithProviders(<App />);
    expect(await screen.findByText('Total Migrations')).toBeInTheDocument();
  });

  it('renders recent migrations table', async () => {
    renderWithProviders(<App />);
    expect(await screen.findByText('Recent Migrations')).toBeInTheDocument();
  });
});