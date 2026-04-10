import { useAuth } from '../context/AuthContext';
import Sidebar from '../components/Sidebar';

const Perfil = () => {
  const { admin } = useAuth();

  return (
    <div style={{ display: 'flex', minHeight: '100vh', backgroundColor: '#0f3460' }}>
      <Sidebar />

      <div style={{ marginLeft: '240px', flex: 1, padding: '32px' }}>
        <h2 style={{ color: 'white', fontSize: '28px', marginBottom: '8px' }}>
          Mi Perfil
        </h2>
        <p style={{ color: '#888', marginBottom: '32px' }}>
          Información de tu cuenta de administrador
        </p>

        <div style={{
          backgroundColor: '#16213e',
          borderRadius: '16px',
          padding: '32px',
          border: '1px solid #2d2d44',
          maxWidth: '500px'
        }}>

          {/* Datos */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>

            <div>
              <p style={{ color: '#888', fontSize: '12px', margin: '0 0 4px 0', textTransform: 'uppercase', letterSpacing: '1px' }}>
                Nombre
              </p>
              <p style={{ color: 'white', fontSize: '18px', fontWeight: 'bold', margin: 0 }}>
                {admin?.nombre}
              </p>
            </div>

            <div style={{ height: '1px', backgroundColor: '#2d2d44' }} />

            <div>
              <p style={{ color: '#888', fontSize: '12px', margin: '0 0 4px 0', textTransform: 'uppercase', letterSpacing: '1px' }}>
                Correo electrónico
              </p>
              <p style={{ color: 'white', fontSize: '16px', margin: 0 }}>
                {admin?.correo}
              </p>
            </div>

            <div style={{ height: '1px', backgroundColor: '#2d2d44' }} />

            <div>
              <p style={{ color: '#888', fontSize: '12px', margin: '0 0 4px 0', textTransform: 'uppercase', letterSpacing: '1px' }}>
                Tipo de administrador
              </p>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <span style={{
                  backgroundColor: admin?.esSuperAdmin ? '#e9456030' :
                                   admin?.tipoAdmin === 'privilegiado' ? '#ff980030' : '#2d2d44',
                  color: admin?.esSuperAdmin ? '#e94560' :
                         admin?.tipoAdmin === 'privilegiado' ? '#ff9800' : '#888',
                  padding: '4px 12px',
                  borderRadius: '12px',
                  fontSize: '14px',
                  fontWeight: 'bold'
                }}>
                  {admin?.esSuperAdmin ? '⭐ Super Admin' :
                   admin?.tipoAdmin === 'privilegiado' ? '🔑 Privilegiado' : '👤 Común'}
                </span>
              </div>
            </div>

            <div style={{ height: '1px', backgroundColor: '#2d2d44' }} />

            <div>
              <p style={{ color: '#888', fontSize: '12px', margin: '0 0 4px 0', textTransform: 'uppercase', letterSpacing: '1px' }}>
                Estado de cuenta
              </p>
              <p style={{ color: '#4caf50', fontSize: '16px', fontWeight: 'bold', margin: 0 }}>
                ● Activo
              </p>
            </div>

          </div>
        </div>
      </div>
    </div>
  );
};

export default Perfil;