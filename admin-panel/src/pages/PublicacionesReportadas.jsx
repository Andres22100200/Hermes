import { useState, useEffect } from 'react';
import {
  obtenerPublicacionesReportadas,
  eliminarPublicacion,
  eliminarReporte
} from '../api/adminApi';
import Sidebar from '../components/Sidebar';
import { BASE_URL } from '../api/adminApi';

// Helper para parsear fotos/géneros que pueden venir como string JSON
const parseJSON = (val) => {
  if (Array.isArray(val)) return val;
  try { return JSON.parse(val); } catch { return []; }
};

const PublicacionesReportadas = () => {
  const [publicaciones, setPublicaciones] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expandida, setExpandida] = useState(null);
  const [error, setError] = useState('');
  const [modalPub, setModalPub] = useState(null);

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
            {publicaciones.map(pub => {
              const fotos = parseJSON(pub.fotos);
              return (
                <div key={pub.id} style={{
                  backgroundColor: '#16213e',
                  borderRadius: '16px',
                  border: '1px solid #2d2d44',
                  overflow: 'hidden'
                }}>
                  <div style={{ padding: '20px', display: 'flex', gap: '16px', alignItems: 'flex-start' }}>

                    {/* Foto */}
                    <img
                      src={fotos[0]
                        ? `${BASE_URL}/uploads/book-pictures/${fotos[0]}`
                        : 'https://via.placeholder.com/80x100?text=Sin+foto'}
                      alt={pub.titulo}
                      style={{
                        width: '80px', height: '100px',
                        objectFit: 'cover', borderRadius: '8px', flexShrink: 0
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

                        <button
                          onClick={() => setModalPub(pub)}
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
                          📖 Ver info completa
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
                              🗑️ Eliminar reporte
                            </button>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Modal info completa */}
      {modalPub && (() => {
        const fotosModal = parseJSON(modalPub.fotos);
        const generosModal = parseJSON(modalPub.generos);
        return (
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
              maxWidth: '600px',
              width: '90%',
              maxHeight: '80vh',
              overflowY: 'auto',
              border: '1px solid #2d2d44'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '24px' }}>
                <h3 style={{ color: 'white', margin: 0 }}>Detalle de Publicación</h3>
                <button
                  onClick={() => setModalPub(null)}
                  style={{
                    backgroundColor: 'transparent',
                    border: 'none',
                    color: '#e94560',
                    fontSize: '24px',
                    cursor: 'pointer'
                  }}
                >✕</button>
              </div>

              {/* Fotos */}
              <div style={{ display: 'flex', gap: '8px', marginBottom: '16px', overflowX: 'auto' }}>
                {fotosModal.map((foto, i) => (
                  <img key={i}
                    src={`${BASE_URL}/uploads/book-pictures/${foto}`}
                    alt={`Foto ${i+1}`}
                    style={{ width: '100px', height: '130px', objectFit: 'cover', borderRadius: '8px', flexShrink: 0 }}
                    onError={e => e.target.src = 'https://via.placeholder.com/100x130?text=Sin+foto'}
                  />
                ))}
              </div>

              {/* Info */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                {[
                  ['Título', modalPub.titulo],
                  ['Autor', modalPub.autor],
                  ['Editorial', modalPub.editorial || 'N/A'],
                  ['Año', modalPub.yearPublicacion || 'N/A'],
                  ['Géneros', generosModal.join(', ') || 'N/A'],
                  ['Estado del libro', modalPub.estadoLibro],
                  ['Precio', `$${modalPub.precio}`],
                  ['Punto de encuentro', modalPub.puntoEncuentro],
                  ['Estado publicación', modalPub.estado],
                  ['Vendedor', `${modalPub.vendedor?.nombre} ${modalPub.vendedor?.apellido}`],
                  ['Descripción', modalPub.descripcion || 'Sin descripción'],
                ].map(([label, value]) => (
                  <div key={label} style={{ borderBottom: '1px solid #2d2d44', paddingBottom: '8px' }}>
                    <p style={{ color: '#888', fontSize: '12px', margin: '0 0 4px 0' }}>{label}</p>
                    <p style={{ color: 'white', fontSize: '14px', margin: 0 }}>{value}</p>
                  </div>
                ))}
              </div>
            </div>
          </div>
        );
      })()}
    </div>
  );
};

export default PublicacionesReportadas;