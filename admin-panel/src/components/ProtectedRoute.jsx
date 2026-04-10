import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const ProtectedRoute = ({ children, soloPrivilegiado = false }) => {
  const { admin, loading } = useAuth();

  if (loading) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh',
        fontSize: '18px'
      }}>
        Cargando...
      </div>
    );
  }

  if (!admin) {
    return <Navigate to="/login" replace />;
  }

  if (soloPrivilegiado && !admin.esSuperAdmin) {
    return <Navigate to="/" replace />;
  }

  return children;
};

export default ProtectedRoute;