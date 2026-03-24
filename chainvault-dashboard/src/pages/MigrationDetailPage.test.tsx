/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import MigrationDetailPage from './MigrationDetailPage';

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useParams: () => ({ id: 'DOC-INV-2026-001' }),
  };
});

const renderWithRouter = (ui: React.ReactElement) => {
  return render(
    <MemoryRouter initialEntries={['/migration/DOC-INV-2026-001']}>
      {ui}
    </MemoryRouter>
  );
};

describe('MigrationDetailPage', () => {
  it('renders migration title', async () => {
    renderWithRouter(<MigrationDetailPage />);
    expect(await screen.findByText(/Migration DOC-INV-2026-001/i)).toBeInTheDocument();
  });

  it('shows status badge', async () => {
    renderWithRouter(<MigrationDetailPage />);
    expect(await screen.findByText('SUCCESS')).toBeInTheDocument();
  });
});