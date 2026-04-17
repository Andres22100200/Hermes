import { useState, useEffect } from 'react';
import {
  obtenerUsuariosReportados,
  banearUsuario,
  banearUsuarioPermanente,
  eliminarReporte,
  revocarSuspension
} from '../api/adminApi';
import Sidebar from '../components/Sidebar';
import { BASE_URL } from '../api/adminApi';

const UsuariosReportados = () => {
  const [usuarios, setUsuarios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expandido, setExpandido] = useState(null);
  const [error, setError] = useState('');
  const [modalUsuario, setModalUsuario] = useState(null);

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
    if (!confirm(`¿Banear a este usuario por ${dias} días?`)) return;
    try {
        await banearUsuario(id, dias, '');
        alert(`Usuario baneado por ${dias} días`);
        cargarUsuarios();
    } catch (err) {
        alert(err.response?.data?.error || 'Error al banear usuario');
    }
};

const handleBanearPermanente = async (id, nombre) => {
    if (!confirm(`¿Eliminar permanentemente la cuenta de ${nombre}? Esta acción no se puede deshacer.`)) return;
    try {
        await banearUsuarioPermanente(id, '');
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

  const handleRevocarSuspension = async (id) => {
    if (!confirm('¿Revocar la suspensión de este usuario?')) return;
    try {
      await revocarSuspension(id);
      alert('Suspensión revocada');
      cargarUsuarios();
    } catch (err) {
      alert('Error al revocar suspensión');
    }
  };

  const parseGenerosUsuario = (generosPreferidos) => {
    if (!generosPreferidos) return 'N/A';
    if (Array.isArray(generosPreferidos)) return generosPreferidos.join(', ');
    try { return JSON.parse(generosPreferidos).join(', '); }
    catch { return String(generosPreferidos); }
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
                <div style={{ padding: '20px', display: 'flex', gap: '16px', alignItems: 'flex-start' }}>

                  <img
                    src={usuario.fotoPerfil
                      ? `${BASE_URL}/uploads/profile-pictures/${usuario.fotoPerfil}`
                      : 'https://via.placeholder.com/60?text=U'}
                    alt={usuario.nombre}
                    style={{
                      width: '60px', height: '60px',
                      objectFit: 'cover', borderRadius: '50%', flexShrink: 0
                    }}
                    onError={e => e.target.src = 'https://via.placeholder.com/60?text=U'}
                  />

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
                            color: !usuario.activo ? '#e94560' :
                                   usuario.suspendidoHasta && new Date(usuario.suspendidoHasta) > new Date()
                                   ? '#ff9800' : '#4caf50',
                            fontWeight: 'bold'
                          }}>
                            {!usuario.activo ? 'Eliminado' :
                             usuario.suspendidoHasta && new Date(usuario.suspendidoHasta) > new Date()
                               ? `Suspendido hasta ${new Date(usuario.suspendidoHasta).toLocaleDateString('es-MX')}`
                               : 'Activo'}
                          </span>
                        </p>
                      </div>

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

                      <button
                        onClick={() => setModalUsuario(usuario)}
                        style={{
                          padding: '8px 16px',
                          backgroundColor: '#0f3460',
                          color: 'white',
                          border: '1px solid #4caf50',
                          borderRadius: '8px',
                          cursor: 'pointer',
                          fontSize: '13px'
                        }}
                      >
                        👤 Ver perfil completo
                      </button>

                      {usuario.activo && (
                        <>
                          {usuario.suspendidoHasta && new Date(usuario.suspendidoHasta) > new Date() ? (
                            <button
                              onClick={() => handleRevocarSuspension(usuario.id)}
                              style={{
                                padding: '8px 16px',
                                backgroundColor: '#4caf5020',
                                color: '#4caf50',
                                border: '1px solid #4caf50',
                                borderRadius: '8px',
                                cursor: 'pointer',
                                fontSize: '13px'
                              }}
                            >
                              ✅ Revocar suspensión
                            </button>
                          ) : (
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
                            </>
                          )}

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
                          alignItems: 'center'
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
                          </div>

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
                            🗑️ Eliminar reporte
                          </button>
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

      {/* Modal perfil completo */}
      {modalUsuario && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.8)',
          display: 'flex', justifyContent: 'center', alignItems: 'center',
          zIndex: 1000
        }}>
          <div style={{
            backgroundColor: '#16213e',
            borderRadius: '16px',
            padding: '32px',
            maxWidth: '500px',
            width: '90%',
            maxHeight: '80vh',
            overflowY: 'auto',
            border: '1px solid #2d2d44'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '24px' }}>
              <h3 style={{ color: 'white', margin: 0 }}>Perfil de Usuario</h3>
              <button
                onClick={() => setModalUsuario(null)}
                style={{
                  backgroundColor: 'transparent',
                  border: 'none',
                  color: '#e94560',
                  fontSize: '24px',
                  cursor: 'pointer'
                }}
              >✕</button>
            </div>

            {/* Foto y nombre */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '24px' }}>
              <img
                src={modalUsuario.fotoPerfil
                  ? `${BASE_URL}/uploads/profile-pictures/${modalUsuario.fotoPerfil}`
                  : 'https://via.placeholder.com/80?text=U'}
                alt={modalUsuario.nombre}
                style={{
                  width: '80px', height: '80px',
                  objectFit: 'cover', borderRadius: '50%'
                }}
                onError={e => e.target.src = 'https://via.placeholder.com/80?text=U'}
              />
              <div>
                <h4 style={{ color: 'white', margin: '0 0 4px 0', fontSize: '20px' }}>
                  {modalUsuario.nombre} {modalUsuario.apellido}
                </h4>
                <span style={{
                  backgroundColor: !modalUsuario.activo ? '#e9456030' :
                                   modalUsuario.suspendidoHasta && new Date(modalUsuario.suspendidoHasta) > new Date()
                                   ? '#ff980030' : '#4caf5030',
                  color: !modalUsuario.activo ? '#e94560' :
                         modalUsuario.suspendidoHasta && new Date(modalUsuario.suspendidoHasta) > new Date()
                         ? '#ff9800' : '#4caf50',
                  padding: '4px 12px',
                  borderRadius: '12px',
                  fontSize: '13px'
                }}>
                  {!modalUsuario.activo ? 'Eliminado' :
                   modalUsuario.suspendidoHasta && new Date(modalUsuario.suspendidoHasta) > new Date()
                     ? 'Suspendido' : 'Activo'}
                </span>
              </div>
            </div>

            {/* Info */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {[
                ['Correo', modalUsuario.correo],
                ['Teléfono', modalUsuario.numeroTelefonico || 'N/A'],
                ['Fecha de nacimiento', modalUsuario.fechaNacimiento
                  ? new Date(modalUsuario.fechaNacimiento).toLocaleDateString('es-MX')
                  : 'N/A'],
                ['Sexo', modalUsuario.sexo || 'N/A'],
                ['Géneros preferidos', parseGenerosUsuario(modalUsuario.generosPreferidos)],
                ['Biografía', modalUsuario.biografia || 'Sin biografía'],
                ['⭐ Como vendedor', `${modalUsuario.promedioEstrellas_vendedor || '0.0'} (${modalUsuario.totalValoraciones_vendedor || 0} valoraciones)`],
                ['⭐ Como comprador', `${modalUsuario.promedioEstrellas_comprador || '0.0'} (${modalUsuario.totalValoraciones_comprador || 0} valoraciones)`],
                ['Motivo de suspensión', modalUsuario.motivoSuspension || 'N/A'],
                ['Suspendido hasta', modalUsuario.suspendidoHasta
                  ? new Date(modalUsuario.suspendidoHasta).toLocaleDateString('es-MX')
                  : 'N/A'],
                ['Miembro desde', new Date(modalUsuario.createdAt).toLocaleDateString('es-MX')],
              ].map(([label, value]) => (
                <div key={label} style={{ borderBottom: '1px solid #2d2d44', paddingBottom: '8px' }}>
                  <p style={{ color: '#888', fontSize: '12px', margin: '0 0 4px 0' }}>{label}</p>
                  <p style={{ color: 'white', fontSize: '14px', margin: 0 }}>{value}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default UsuariosReportados;