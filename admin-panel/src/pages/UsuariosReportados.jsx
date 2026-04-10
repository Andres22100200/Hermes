import { useState, useEffect } from 'react';
import {
  obtenerUsuariosReportados,
  banearUsuario,
  banearUsuarioPermanente,
  eliminarReporte,
  cerrarReporte
} from '../api/adminApi';
import Sidebar from '../components/Sidebar';

const BASE_URL = 'http://192.168.100.5:3000';

const UsuariosReportados = () => {
  const [usuarios, setUsuarios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expandido, setExpandido] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    cargarUsuarios();
  }, []);

  const cargarUsuarios = async () => {
    try {
      setLoading(true);
      const res = await obtenerUsuariosReportados();
      setUsuarios(res.data.usuarios);
    } catch (err) {
      setError('Error al cargar usuarios reportados');
    } finally {
      setLoading(false);
    }
  };

  const handleBanear = async (id, dias) => {
    const motivo = prompt(`Motivo del ban de ${dias} días (opcional):`);
    if (motivo === null) return;
    try {
      await banearUsuario(id, dias, motivo);
      alert(`Usuario baneado por ${dias} días`);
      cargarUsuarios();
    } catch (err) {
      alert(err.response?.data?.error || 'Error al banear usuario');
    }
  };

  const handleBanearPermanente = async (id, nombre) => {
    if (!confirm(`¿Eliminar permanentemente la cuenta de ${nombre}? Esta acción no se puede deshacer.`)) return;
    const motivo = prompt('Motivo (opcional):');
    if (motivo === null) return;
    try {
      await banearUsuarioPermanente(id, motivo);
      alert('Cuenta eliminada permanentemente');
      cargarUsuarios();
    } catch (err) {
      alert(err.response?.data?.error || 'Error al eliminar cuenta');
    }
  };

  const handleEliminarReporte = async (reporteId, usuarioId) => {
    if (!confirm('¿Eliminar este reporte?')) return;
    try {
      await eliminarReporte(reporteId);
      setUsuarios(prev => prev.map(u => {
        if (u.id === usuarioId) {
          return {
            ...u,
            reportesRecibidos: u.reportesRecibidos.filter(r => r.id !== reporteId),
            totalReportes: u.totalReportes - 1
          };
        }
        return u;
      }).filter(u => u.totalReportes > 0));
    } catch (err) {
      alert('Error al eliminar reporte');
    }
  };

  const handleCerrarReporte = async (reporteId, usuarioId) => {
    try {
      await cerrarReporte(reporteId);
      setUsuarios(prev => prev.map(u => {
        if (u.id === usuarioId) {
          return {
            ...u,
            reportesRecibidos: u.reportesRecibidos.map(r =>
              r.id === reporteId ? { ...r, procesado: true } : r
            )
          };
        }
        return u;
      }));
    } catch (err) {
      alert('Error al cerrar reporte');
    }
  };

  return (
    <div style={{ display: 'flex', minHeight: '100vh', backgroundColor: '#0f3460' }}>
      <Sidebar />

      <div style={{ marginLeft: '240px', flex: 1, padding: '32px' }}>
        <h2 style={{ color: 'white', fontSize: '28px', marginBottom: '8px' }}>
          Usuarios Reportados
        </h2>
        <p style={{ color: '#888', marginBottom: '32px' }}>
          {usuarios.length} usuario(s) con reportes vigentes
        </p>

        {error && (
          <div style={{
            backgroundColor: '#e9456020',
            border: '1px solid #e94560',
            color: '#e94560',
            padding: '12px',
            borderRadius: '8px',
            marginBottom: '16px'
          }}>
            {error}
          </div>
        )}

        {loading ? (
          <p style={{ color: '#888' }}>Cargando...</p>
        ) : usuarios.length === 0 ? (
          <div style={{
            backgroundColor: '#16213e',
            borderRadius: '16px',
            padding: '48px',
            textAlign: 'center'
          }}>
            <p style={{ color: '#888', fontSize: '18px' }}>
              ✅ No hay usuarios reportados
            </p>
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {usuarios.map(usuario => (
              <div key={usuario.id} style={{
                backgroundColor: '#16213e',
                borderRadius: '16px',
                border: '1px solid #2d2d44',
                overflow: 'hidden'
              }}>
                {/* Cabecera del usuario */}
                <div style={{ padding: '20px', display: 'flex', gap: '16px', alignItems: 'flex-start' }}>

                  {/* Foto */}
                  <img
                    src={usuario.fotoPerfil
                      ? `${BASE_URL}/uploads/profile-pictures/${usuario.fotoPerfil}`
                      : 'https://via.placeholder.com/60?text=👤'}
                    alt={usuario.nombre}
                    style={{
                      width: '60px',
                      height: '60px',
                      objectFit: 'cover',
                      borderRadius: '50%',
                      flexShrink: 0
                    }}
                    onError={e => e.target.src = 'https://via.placeholder.com/60?text=👤'}
                  />

                  {/* Info */}
                  <div style={{ flex: 1 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                      <div>
                        <h3 style={{ color: 'white', margin: '0 0 4px 0', fontSize: '18px' }}>
                          {usuario.nombre} {usuario.apellido}
                        </h3>
                        <p style={{ color: '#888', margin: '0 0 4px 0', fontSize: '14px' }}>
                          {usuario.correo}
                        </p>
                        <p style={{ color: '#888', margin: '0', fontSize: '13px' }}>
                          ⭐ {usuario.promedioEstrellas_vendedor || '0.0'} como vendedor
                        </p>
                        <p style={{ color: '#888', margin: '4px 0 0 0', fontSize: '13px' }}>
                          Estado: <span style={{
                            color: usuario.activo ? '#4caf50' : '#e94560',
                            fontWeight: 'bold'
                          }}>
                            {usuario.activo
                              ? usuario.suspendidoHasta && new Date(usuario.suspendidoHasta) > new Date()
                                ? `Suspendido hasta ${new Date(usuario.suspendidoHasta).toLocaleDateString('es-MX')}`
                                : 'Activo'
                              : 'Eliminado'}
                          </span>
                        </p>
                      </div>

                      {/* Badge reportes */}
                      <div style={{
                        backgroundColor: usuario.totalReportes >= 10 ? '#e94560' :
                                         usuario.totalReportes >= 5 ? '#ff9800' : '#2d2d44',
                        color: 'white',
                        padding: '8px 16px',
                        borderRadius: '20px',
                        fontSize: '14px',
                        fontWeight: 'bold',
                        flexShrink: 0
                      }}>
                        🚩 {usuario.totalReportes} reporte(s)
                      </div>
                    </div>

                    {/* Acciones */}
                    <div style={{ display: 'flex', gap: '8px', marginTop: '16px', flexWrap: 'wrap' }}>
                      <button
                        onClick={() => setExpandido(expandido === usuario.id ? null : usuario.id)}
                        style={{
                          padding: '8px 16px',
                          backgroundColor: '#0f3460',
                          color: 'white',
                          border: '1px solid #2d2d44',
                          borderRadius: '8px',
                          cursor: 'pointer',
                          fontSize: '13px'
                        }}
                      >
                        {expandido === usuario.id ? '▲ Ocultar reportes' : '▼ Ver reportes'}
                      </button>

                      {usuario.activo && (
                        <>
                          <button
                            onClick={() => handleBanear(usuario.id, 2)}
                            style={{
                              padding: '8px 16px',
                              backgroundColor: '#ff980020',
                              color: '#ff9800',
                              border: '1px solid #ff9800',
                              borderRadius: '8px',
                              cursor: 'pointer',
                              fontSize: '13px'
                            }}
                          >
                            🔒 Banear 2 días
                          </button>

                          <button
                            onClick={() => handleBanear(usuario.id, 7)}
                            style={{
                              padding: '8px 16px',
                              backgroundColor: '#ff980020',
                              color: '#ff9800',
                              border: '1px solid #ff9800',
                              borderRadius: '8px',
                              cursor: 'pointer',
                              fontSize: '13px'
                            }}
                          >
                            🔒 Banear 7 días
                          </button>

                          <button
                            onClick={() => handleBanearPermanente(usuario.id, `${usuario.nombre} ${usuario.apellido}`)}
                            style={{
                              padding: '8px 16px',
                              backgroundColor: '#e94560',
                              color: 'white',
                              border: 'none',
                              borderRadius: '8px',
                              cursor: 'pointer',
                              fontSize: '13px',
                              fontWeight: 'bold'
                            }}
                          >
                            🗑️ Eliminar cuenta
                          </button>
                        </>
                      )}
                    </div>
                  </div>
                </div>

                {/* Reportes expandidos */}
                {expandido === usuario.id && (
                  <div style={{
                    borderTop: '1px solid #2d2d44',
                    padding: '16px 20px',
                    backgroundColor: '#0f3460'
                  }}>
                    <h4 style={{ color: 'white', margin: '0 0 16px 0', fontSize: '15px' }}>
                      Reportes ({usuario.reportesRecibidos.length})
                    </h4>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                      {usuario.reportesRecibidos.map(reporte => (
                        <div key={reporte.id} style={{
                          backgroundColor: '#16213e',
                          borderRadius: '8px',
                          padding: '12px 16px',
                          display: 'flex',
                          justifyContent: 'space-between',
                          alignItems: 'center',
                          opacity: reporte.procesado ? 0.5 : 1
                        }}>
                          <div>
                            <span style={{
                              backgroundColor: '#2d2d44',
                              color: '#ccc',
                              padding: '4px 10px',
                              borderRadius: '12px',
                              fontSize: '12px',
                              marginRight: '8px'
                            }}>
                              {reporte.categoria}
                            </span>
                            <span style={{ color: '#888', fontSize: '12px' }}>
                              {new Date(reporte.createdAt).toLocaleDateString('es-MX')}
                            </span>
                            {reporte.procesado && (
                              <span style={{ color: '#4caf50', fontSize: '12px', marginLeft: '8px' }}>
                                ✓ Cerrado
                              </span>
                            )}
                          </div>

                          {!reporte.procesado && (
                            <div style={{ display: 'flex', gap: '8px' }}>
                              <button
                                onClick={() => handleCerrarReporte(reporte.id, usuario.id)}
                                style={{
                                  padding: '6px 12px',
                                  backgroundColor: '#4caf5020',
                                  color: '#4caf50',
                                  border: '1px solid #4caf50',
                                  borderRadius: '6px',
                                  cursor: 'pointer',
                                  fontSize: '12px'
                                }}
                              >
                                ✓ Cerrar
                              </button>
                              <button
                                onClick={() => handleEliminarReporte(reporte.id, usuario.id)}
                                style={{
                                  padding: '6px 12px',
                                  backgroundColor: '#e9456020',
                                  color: '#e94560',
                                  border: '1px solid #e94560',
                                  borderRadius: '6px',
                                  cursor: 'pointer',
                                  fontSize: '12px'
                                }}
                              >
                                🗑️ Eliminar
                              </button>
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default UsuariosReportados;