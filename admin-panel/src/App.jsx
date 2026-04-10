import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';

import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import PublicacionesReportadas from './pages/PublicacionesReportadas';
import UsuariosReportados from './pages/UsuariosReportados';
import GestionAdmins from './pages/GestionAdmins';
import Perfil from './pages/Perfil';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter basename="/admin">
        <Routes>
          <Route path="/login" element={<Login />} />

          <Route path="/" element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          } />

          <Route path="/publicaciones-reportadas" element={
            <ProtectedRoute>
              <PublicacionesReportadas />
            </ProtectedRoute>
          } />

          <Route path="/usuarios-reportados" element={
            <ProtectedRoute>
              <UsuariosReportados />
            </ProtectedRoute>
          } />

          <Route path="/admins" element={
            <ProtectedRoute soloPrivilegiado={true}>
              <GestionAdmins />
            </ProtectedRoute>
          } />

          <Route path="/perfil" element={
            <ProtectedRoute>
              <Perfil />
            </ProtectedRoute>
          } />

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;