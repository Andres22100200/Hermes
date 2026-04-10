import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Sidebar = () => {
  const { admin, logout } = useAuth();

  return (
    <div style={{
      width: '240px',
      minHeight: '100vh',
      backgroundColor: '#1a1a2e',
      color: 'white',
      display: 'flex',
      flexDirection: 'column',
      padding: '24px 0',
      position: 'fixed',
      left: 0,
      top: 0
    }}>
      {/* Logo */}
      <div style={{ padding: '0 24px 32px 24px', borderBottom: '1px solid #2d2d44' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
  <img src="/hermes_icon.png" alt="Hermes" style={{ width: '32px', height: '32px' }} />
  <h1 style={{ fontSize: '22px', fontWeight: 'bold', color: '#e94560', margin: 0 }}>
    Hermes
  </h1>
</div>
        <p style={{ fontSize: '12px', color: '#888', margin: '4px 0 0 0' }}>
          Panel Admin
        </p>
      </div>

      {/* Info del admin */}
      <div style={{ padding: '16px 24px', borderBottom: '1px solid #2d2d44' }}>
        <p style={{ margin: 0, fontSize: '14px', fontWeight: 'bold' }}>{admin?.nombre}</p>
        <p style={{ margin: '4px 0 0 0', fontSize: '12px', color: '#888' }}>
          {admin?.esSuperAdmin ? '⭐ Super Admin' : admin?.tipoAdmin === 'privilegiado' ? '🔑 Privilegiado' : '👤 Común'}
        </p>
      </div>

      {/* Navegación */}
      <nav style={{ flex: 1, padding: '16px 0' }}>
        <NavLink to="/" end style={({ isActive }) => ({
          display: 'flex',
          alignItems: 'center',
          gap: '12px',
          padding: '12px 24px',
          color: isActive ? '#e94560' : '#ccc',
          textDecoration: 'none',
          backgroundColor: isActive ? '#2d2d44' : 'transparent',
          borderLeft: isActive ? '3px solid #e94560' : '3px solid transparent',
          fontSize: '14px'
        })}>
          📊 Dashboard
        </NavLink>

        <NavLink to="/publicaciones-reportadas" style={({ isActive }) => ({
          display: 'flex',
          alignItems: 'center',
          gap: '12px',
          padding: '12px 24px',
          color: isActive ? '#e94560' : '#ccc',
          textDecoration: 'none',
          backgroundColor: isActive ? '#2d2d44' : 'transparent',
          borderLeft: isActive ? '3px solid #e94560' : '3px solid transparent',
          fontSize: '14px'
        })}>
          📚 Publicaciones Reportadas
        </NavLink>

        <NavLink to="/usuarios-reportados" style={({ isActive }) => ({
          display: 'flex',
          alignItems: 'center',
          gap: '12px',
          padding: '12px 24px',
          color: isActive ? '#e94560' : '#ccc',
          textDecoration: 'none',
          backgroundColor: isActive ? '#2d2d44' : 'transparent',
          borderLeft: isActive ? '3px solid #e94560' : '3px solid transparent',
          fontSize: '14px'
        })}>
          👤 Usuarios Reportados
        </NavLink>

        {/* Solo para admins privilegiados */}
        {(admin?.esSuperAdmin || admin?.tipoAdmin === 'privilegiado') && (
          <NavLink to="/admins" style={({ isActive }) => ({
            display: 'flex',
            alignItems: 'center',
            gap: '12px',
            padding: '12px 24px',
            color: isActive ? '#e94560' : '#ccc',
            textDecoration: 'none',
            backgroundColor: isActive ? '#2d2d44' : 'transparent',
            borderLeft: isActive ? '3px solid #e94560' : '3px solid transparent',
            fontSize: '14px'
          })}>
            ⚙️ Gestión de Admins
          </NavLink>
        )}

        <NavLink to="/perfil" style={({ isActive }) => ({
          display: 'flex',
          alignItems: 'center',
          gap: '12px',
          padding: '12px 24px',
          color: isActive ? '#e94560' : '#ccc',
          textDecoration: 'none',
          backgroundColor: isActive ? '#2d2d44' : 'transparent',
          borderLeft: isActive ? '3px solid #e94560' : '3px solid transparent',
          fontSize: '14px'
        })}>
          👤 Mi Perfil
        </NavLink>
      </nav>

      {/* Cerrar sesión */}
      <div style={{ padding: '16px 24px', borderTop: '1px solid #2d2d44' }}>
        <button onClick={logout} style={{
          width: '100%',
          padding: '10px',
          backgroundColor: '#e94560',
          color: 'white',
          border: 'none',
          borderRadius: '8px',
          cursor: 'pointer',
          fontSize: '14px',
          fontWeight: 'bold'
        }}>
          Cerrar Sesión
        </button>
      </div>
    </div>
  );
};

export default Sidebar;