import { useState, useEffect } from 'react';
import {
  obtenerPublicacionesReportadas,
  eliminarPublicacion,
  eliminarReporte,
  cerrarReporte
} from '../api/adminApi';
import Sidebar from '../components/Sidebar';

const BASE_URL = 'http://192.168.100.5:3000';

const PublicacionesReportadas = () => {
  const [publicaciones, setPublicaciones] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expandida, setExpandida] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    cargarPublicaciones();
  }, []);

  const cargarPublicaciones = async () => {
    try {
      setLoading(true);
      const res = await obtenerPublicacionesReportadas();
      setPublicaciones(res.data.publicaciones);
    } catch (err) {
      setError('Error al cargar publicaciones reportadas');
    } finally {
      setLoading(false);
    }
  };

  const handleEliminarPublicacion = async (id, titulo) => {
    if (!confirm(`¿Eliminar la publicación "${titulo}"?`)) return;
    try {
      await eliminarPublicacion(id);
      setPublicaciones(prev => prev.filter(p => p.id !== id));
    } catch (err) {
      alert('Error al eliminar publicación');
    }
  };

  const handleEliminarReporte = async (reporteId, publicacionId) => {
    if (!confirm('¿Eliminar este reporte?')) return;
    try {
      await eliminarReporte(reporteId);
      setPublicaciones(prev => prev.map(p => {
        if (p.id === publicacionId) {
          return {
            ...p,
            reportes: p.reportes.filter(r => r.id !== reporteId),
            totalReportes: p.totalReportes - 1
          };
        }
        return p;
      }).filter(p => p.totalReportes > 0));
    } catch (err) {
      alert('Error al eliminar reporte');
    }
  };

  const handleCerrarReporte = async (reporteId, publicacionId) => {
    try {
      await cerrarReporte(reporteId);
      setPublicaciones(prev => prev.map(p => {
        if (p.id === publicacionId) {
          return {
            ...p,
            reportes: p.reportes.map(r =>
              r.id === reporteId ? { ...r, procesado: true } : r
            )
          };
        }
        return p;
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
          Publicaciones Reportadas
        </h2>
        <p style={{ color: '#888', marginBottom: '32px' }}>
          {publicaciones.length} publicación(es) con reportes vigentes
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
        ) : publicaciones.length === 0 ? (
          <div style={{
            backgroundColor: '#16213e',
            borderRadius: '16px',
            padding: '48px',
            textAlign: 'center'
          }}>
            <p style={{ color: '#888', fontSize: '18px' }}>
              ✅ No hay publicaciones reportadas
            </p>
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {publicaciones.map(pub => (
              <div key={pub.id} style={{
                backgroundColor: '#16213e',
                borderRadius: '16px',
                border: '1px solid #2d2d44',
                overflow: 'hidden'
              }}>
                {/* Cabecera de la publicación */}
                <div style={{ padding: '20px', display: 'flex', gap: '16px', alignItems: 'flex-start' }}>
                  
                  {/* Foto */}
                  <img
                    src={pub.fotos?.[0]
                      ? `${BASE_URL}/uploads/book-pictures/${pub.fotos[0]}`
                      : 'https://via.placeholder.com/80x100?text=Sin+foto'}
                    alt={pub.titulo}
                    style={{
                      width: '80px',
                      height: '100px',
                      objectFit: 'cover',
                      borderRadius: '8px',
                      flexShrink: 0
                    }}
                    onError={e => e.target.src = 'https://via.placeholder.com/80x100?text=Sin+foto'}
                  />

                  {/* Info */}
                  <div style={{ flex: 1 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                      <div>
                        <h3 style={{ color: 'white', margin: '0 0 4px 0', fontSize: '18px' }}>
                          {pub.titulo}
                        </h3>
                        <p style={{ color: '#888', margin: '0 0 4px 0', fontSize: '14px' }}>
                          {pub.autor} {pub.editorial ? `· ${pub.editorial}` : ''}
                        </p>
                        <p style={{ color: '#888', margin: '0 0 8px 0', fontSize: '13px' }}>
                          Vendedor: <span style={{ color: '#ccc' }}>
                            {pub.vendedor?.nombre} {pub.vendedor?.apellido}
                          </span>
                        </p>
                        <p style={{ color: '#888', margin: '0', fontSize: '13px' }}>
                          Estado: <span style={{
                            color: pub.estado === 'Disponible' ? '#4caf50' :
                                   pub.estado === 'Eliminado' ? '#e94560' : '#ff9800',
                            fontWeight: 'bold'
                          }}>
                            {pub.estado}
                          </span>
                        </p>
                      </div>

                      {/* Badge de reportes */}
                      <div style={{
                        backgroundColor: pub.totalReportes >= 10 ? '#e94560' :
                                         pub.totalReportes >= 5 ? '#ff9800' : '#2d2d44',
                        color: 'white',
                        padding: '8px 16px',
                        borderRadius: '20px',
                        fontSize: '14px',
                        fontWeight: 'bold',
                        flexShrink: 0
                      }}>
                        🚩 {pub.totalReportes} reporte(s)
                      </div>
                    </div>

                    {/* Acciones */}
                    <div style={{ display: 'flex', gap: '8px', marginTop: '16px', flexWrap: 'wrap' }}>
                      <button
                        onClick={() => setExpandida(expandida === pub.id ? null : pub.id)}
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
                        {expandida === pub.id ? '▲ Ocultar reportes' : '▼ Ver reportes'}
                      </button>

                      {pub.estado !== 'Eliminado' && (
                        <button
                          onClick={() => handleEliminarPublicacion(pub.id, pub.titulo)}
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
                          🗑️ Eliminar publicación
                        </button>
                      )}
                    </div>
                  </div>
                </div>

                {/* Reportes expandidos */}
                {expandida === pub.id && (
                  <div style={{
                    borderTop: '1px solid #2d2d44',
                    padding: '16px 20px',
                    backgroundColor: '#0f3460'
                  }}>
                    <h4 style={{ color: 'white', margin: '0 0 16px 0', fontSize: '15px' }}>
                      Reportes ({pub.reportes.length})
                    </h4>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                      {pub.reportes.map(reporte => (
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
                                onClick={() => handleCerrarReporte(reporte.id, pub.id)}
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
                                onClick={() => handleEliminarReporte(reporte.id, pub.id)}
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

export default PublicacionesReportadas;