import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { Navbar } from '@/components/layout/Navbar'
import { ProtectedRoute } from '@/components/layout/ProtectedRoute'
import { LoginPage } from '@/pages/LoginPage'
import { RegisterPage } from '@/pages/RegisterPage'
import { PokemonBrowserPage } from '@/pages/PokemonBrowserPage'
import { TeamListPage } from '@/pages/TeamListPage'
import { TeamBuilderPage } from '@/pages/TeamBuilderPage'
import { LobbyPage } from '@/pages/LobbyPage'
import { BattlePage } from '@/pages/BattlePage'
import { ProfilePage } from '@/pages/ProfilePage'

export function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-base flex flex-col">
        <Navbar />
        <main className="flex-1">
          <Routes>
            {/* Public routes */}
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            {/* Protected routes */}
            <Route element={<ProtectedRoute />}>
              <Route path="/pokemon" element={<PokemonBrowserPage />} />
              <Route path="/teams" element={<TeamListPage />} />
              <Route path="/teams/new" element={<TeamBuilderPage />} />
              <Route path="/lobby" element={<LobbyPage />} />
              <Route path="/battle/:id" element={<BattlePage />} />
              <Route path="/profile" element={<ProfilePage />} />
            </Route>

            {/* Default redirect */}
            <Route path="*" element={<Navigate to="/pokemon" replace />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  )
}
