import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { obtenerPublicacionesReportadas, obtenerUsuariosReportados } from '../api/adminApi';
import Sidebar from '../components/Sidebar';

const Dashboard = () => {
  const [stats, setStats] = useState({
    publicacionesReportadas: 0,
    usuariosReportados: 0
  });
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const cargarStats = async () => {
      try {
        const [pubRes, usrRes] = await Promise.all([
          obtenerPublicacionesReportadas(),
          obtenerUsuariosReportados()
        ]);
        setStats({
          publicacionesReportadas: pubRes.data.total,
          usuariosReportados: usrRes.data.total
        });
      } catch (err) {
        console.error('Error al cargar stats:', err);
      } finally {
        setLoading(false);
      }
    };
    cargarStats();
  }, []);

  return (
    <div style={{ display: 'flex', minHeight: '100vh', backgroundColor: '#0f3460' }}>
      <Sidebar />

      <div style={{ marginLeft: '240px', flex: 1, padding: '32px' }}>
        <h2 style={{ color: 'white', fontSize: '28px', marginBottom: '8px' }}>Dashboard</h2>
        <p style={{ color: '#888', marginBottom: '32px' }}>
          Resumen general del sistema
        </p>

        {loading ? (
          <p style={{ color: '#888' }}>Cargando...</p>
        ) : (
          <>
            {/* Tarjetas de stats */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '24px', marginBottom: '40px' }}>
              
              {/* Publicaciones reportadas */}
              <div
                onClick={() => navigate('/publicaciones-reportadas')}
                style={{
                  backgroundColor: '#16213e',
                  borderRadius: '16px',
                  padding: '24px',
                  cursor: 'pointer',
                  border: '1px solid #2d2d44',
                  transition: 'border-color 0.2s'
                }}
                onMouseEnter={e => e.currentTarget.style.borderColor = '#e94560'}
                onMouseLeave={e => e.currentTarget.style.borderColor = '#2d2d44'}
              >
                <div style={{ fontSize: '40px', marginBottom: '12px' }}>📚</div>
                <h3 style={{ color: 'white', margin: '0 0 8px 0', fontSize: '18px' }}>
                  Publicaciones Reportadas
                </h3>
                <p style={{
                  color: '#e94560',
                  fontSize: '48px',
                  fontWeight: 'bold',
                  margin: 0
                }}>
                  {stats.publicacionesReportadas}
                </p>
                <p style={{ color: '#888', fontSize: '13px', margin: '8px 0 0 0' }}>
                  Con reportes vigentes
                </p>
              </div>

              {/* Usuarios reportados */}
              <div
                onClick={() => navigate('/usuarios-reportados')}
                style={{
                  backgroundColor: '#16213e',
                  borderRadius: '16px',
                  padding: '24px',
                  cursor: 'pointer',
                  border: '1px solid #2d2d44',
                  transition: 'border-color 0.2s'
                }}
                onMouseEnter={e => e.currentTarget.style.borderColor = '#e94560'}
                onMouseLeave={e => e.currentTarget.style.borderColor = '#2d2d44'}
              >
                <div style={{ fontSize: '40px', marginBottom: '12px' }}>👤</div>
                <h3 style={{ color: 'white', margin: '0 0 8px 0', fontSize: '18px' }}>
                  Usuarios Reportados
                </h3>
                <p style={{
                  color: '#e94560',
                  fontSize: '48px',
                  fontWeight: 'bold',
                  margin: 0
                }}>
                  {stats.usuariosReportados}
                </p>
                <p style={{ color: '#888', fontSize: '13px', margin: '8px 0 0 0' }}>
                  Con reportes vigentes
                </p>
              </div>
            </div>

            {/* Accesos rápidos */}
            <h3 style={{ color: 'white', marginBottom: '16px' }}>Accesos rápidos</h3>
            <div style={{ display: 'flex', gap: '16px' }}>
              <button
                onClick={() => navigate('/publicaciones-reportadas')}
                style={{
                  padding: '12px 24px',
                  backgroundColor: '#e94560',
                  color: 'white',
                  border: 'none',
                  borderRadius: '8px',
                  cursor: 'pointer',
                  fontSize: '14px',
                  fontWeight: 'bold'
                }}
              >
                Ver publicaciones reportadas
              </button>
              <button
                onClick={() => navigate('/usuarios-reportados')}
                style={{
                  padding: '12px 24px',
                  backgroundColor: '#0f3460',
                  color: 'white',
                  border: '1px solid #e94560',
                  borderRadius: '8px',
                  cursor: 'pointer',
                  fontSize: '14px',
                  fontWeight: 'bold'
                }}
              >
                Ver usuarios reportados
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default Dashboard;