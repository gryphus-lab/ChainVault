/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { describe, it, expect, vi } from 'vitest';
import { screen, render } from '@/test/test-utils';
import MigrationDetailPage from './MigrationDetailPage';

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useParams: () => ({ id: 'DOC-INV-2026-001' }),
  };
});

vi.mock('@/lib/api', () => ({
  getMigrationDetail: vi.fn().mockResolvedValue({
    id: "DOC-INV-2026-001",
    title: "Invoice #8742 - Acme Solutions AG",
    status: "SUCCESS",
    createdAt: "2026-03-24T10:15:30Z",
    updatedAt: "2026-03-24T10:18:45Z",
    pageCount: 5,
    ocrAttempted: true,
    ocrSuccess: true,
    events: [],
  }),
}));

describe('MigrationDetailPage', () => {
  it('renders migration title', async () => {
    render(<MigrationDetailPage />);
    expect(await screen.findByText(/Migration DOC-INV-2026-001/i)).toBeInTheDocument();
  });

  it('shows status badge', async () => {
    render(<MigrationDetailPage />);
    expect(await screen.findByText('SUCCESS')).toBeInTheDocument();
  });
});