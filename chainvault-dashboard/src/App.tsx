/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { Routes, Route } from "react-router-dom";
import Overview from "./pages/Overview";
import MigrationDetailPage from "./pages/MigrationDetailPage";

function App() {
  return (
    <div className="min-h-screen bg-gray-50 pb-12">
      <Routes>
        <Route path="/" element={<Overview />} />
        <Route path="/migration/:id" element={<MigrationDetailPage />} />
      </Routes>
    </div>
  );
}

export default App;
