/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Overview from './pages/Overview';
import MigrationDetailPage from './pages/MigrationDetailPage';

function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-gray-50">
        {/* Header */}
        <header className="bg-white shadow">
          <div className="max-w-7xl mx-auto px-6 py-4">
            <h1 className="text-2xl font-bold text-gray-900">ChainVault Migration Dashboard</h1>
          </div>
        </header>

        <main className="max-w-7xl mx-auto py-6 px-6">
          <Routes>
            <Route path="/" element={<Overview />} />
            <Route path="/migration/:id" element={<MigrationDetailPage />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;