import { useState, useEffect } from 'react';
import { listarAdmins, crearAdmin, eliminarAdmin } from '../api/adminApi';
import Sidebar from '../components/Sidebar';
import { useAuth } from '../context/AuthContext';

const GestionAdmins = () => {
  const [admins, setAdmins] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [exito, setExito] = useState('');
  const { admin: adminActual } = useAuth();

  // Formulario nuevo admin
  const [form, setForm] = useState({
    nombre: '',
    correo: '',
    password: '',
    tipoAdmin: 'comun'
  });
  const [loadingForm, setLoadingForm] = useState(false);

  useEffect(() => {
    cargarAdmins();
  }, []);

  const cargarAdmins = async () => {
    try {
      setLoading(true);
      const res = await listarAdmins();
      setAdmins(res.data.administradores);
    } catch (err) {
      setError('Error al cargar administradores');
    } finally {
      setLoading(false);
    }
  };

  const handleCrearAdmin = async (e) => {
    e.preventDefault();
    setError('');
    setExito('');
    setLoadingForm(true);

    try {
      await crearAdmin(form);
      setExito(`Administrador "${form.nombre}" creado exitosamente`);
      setForm({ nombre: '', correo: '', password: '', tipoAdmin: 'comun' });
      cargarAdmins();
    } catch (err) {
      setError(err.response?.data?.error || 'Error al crear administrador');
    } finally {
      setLoadingForm(false);
    }
  };

  const handleEliminarAdmin = async (id, nombre) => {
    if (!confirm(`¿Eliminar al administrador "${nombre}"?`)) return;
    try {
      await eliminarAdmin(id);
      setAdmins(prev => prev.filter(a => a.id !== id));
      setExito(`Administrador "${nombre}" eliminado`);
    } catch (err) {
      setError(err.response?.data?.error || 'Error al eliminar administrador');
    }
  };

  const inputStyle = {
    width: '100%',
    padding: '10px 12px',
    backgroundColor: '#0f3460',
    border: '1px solid #2d2d44',
    borderRadius: '8px',
    color: 'white',
    fontSize: '14px',
    boxSizing: 'border-box'
  };

  const labelStyle = {
    color: '#ccc',
    fontSize: '13px',
    display: 'block',
    marginBottom: '6px'
  };

  return (
    <div style={{ display: 'flex', minHeight: '100vh', backgroundColor: '#0f3460' }}>
      <Sidebar />

      <div style={{ marginLeft: '240px', flex: 1, padding: '32px' }}>
        <h2 style={{ color: 'white', fontSize: '28px', marginBottom: '8px' }}>
          Gestión de Administradores
        </h2>
        <p style={{ color: '#888', marginBottom: '32px' }}>
          Solo visible para administradores privilegiados
        </p>

        {error && (
          <div style={{
            backgroundColor: '#e9456020',
            border: '1px solid #e94560',
            color: '#e94560',
            padding: '12px',
            borderRadius: '8px',
            marginBottom: '16px',
            fontSize: '14px'
          }}>
            {error}
          </div>
        )}

        {exito && (
          <div style={{
            backgroundColor: '#4caf5020',
            border: '1px solid #4caf50',
            color: '#4caf50',
            padding: '12px',
            borderRadius: '8px',
            marginBottom: '16px',
            fontSize: '14px'
          }}>
            {exito}
          </div>
        )}

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '32px' }}>

          {/* Lista de admins */}
          <div>
            <h3 style={{ color: 'white', marginBottom: '16px', fontSize: '18px' }}>
              Administradores ({admins.length})
            </h3>

            {loading ? (
              <p style={{ color: '#888' }}>Cargando...</p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                {admins.map(admin => (
                  <div key={admin.id} style={{
                    backgroundColor: '#16213e',
                    borderRadius: '12px',
                    padding: '16px',
                    border: '1px solid #2d2d44'
                  }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                      <div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                          <p style={{ color: 'white', margin: 0, fontWeight: 'bold', fontSize: '15px' }}>
                            {admin.nombre}
                          </p>
                          {admin.esSuperAdmin && (
                            <span style={{
                              backgroundColor: '#e9456030',
                              color: '#e94560',
                              padding: '2px 8px',
                              borderRadius: '10px',
                              fontSize: '11px'
                            }}>
                              SuperAdmin
                            </span>
                          )}
                          <span style={{
                            backgroundColor: admin.tipoAdmin === 'privilegiado' ? '#ff980030' : '#2d2d44',
                            color: admin.tipoAdmin === 'privilegiado' ? '#ff9800' : '#888',
                            padding: '2px 8px',
                            borderRadius: '10px',
                            fontSize: '11px'
                          }}>
                            {admin.tipoAdmin}
                          </span>
                        </div>
                        <p style={{ color: '#888', margin: '0 0 4px 0', fontSize: '13px' }}>
                          {admin.correo}
                        </p>
                        <p style={{ color: '#888', margin: 0, fontSize: '12px' }}>
                          Registrado: {new Date(admin.createdAt).toLocaleDateString('es-MX')}
                        </p>
                        <p style={{ color: admin.activo ? '#4caf50' : '#e94560', margin: '4px 0 0 0', fontSize: '12px' }}>
                          {admin.activo ? '● Activo' : '● Inactivo'}
                        </p>
                      </div>

                      {/* No se puede eliminar superadmins ni privilegiados ni a uno mismo */}
                      {!admin.esSuperAdmin &&
                       admin.tipoAdmin !== 'privilegiado' &&
                       admin.id !== adminActual?.id && (
                        <button
                          onClick={() => handleEliminarAdmin(admin.id, admin.nombre)}
                          style={{
                            padding: '8px 12px',
                            backgroundColor: '#e9456020',
                            color: '#e94560',
                            border: '1px solid #e94560',
                            borderRadius: '8px',
                            cursor: 'pointer',
                            fontSize: '13px'
                          }}
                        >
                          🗑️ Eliminar
                        </button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Formulario nuevo admin */}
          <div>
            <h3 style={{ color: 'white', marginBottom: '16px', fontSize: '18px' }}>
              Agregar Administrador
            </h3>

            <div style={{
              backgroundColor: '#16213e',
              borderRadius: '12px',
              padding: '24px',
              border: '1px solid #2d2d44'
            }}>
              <form onSubmit={handleCrearAdmin}>
                <div style={{ marginBottom: '16px' }}>
                  <label style={labelStyle}>Nombre completo</label>
                  <input
                    type="text"
                    value={form.nombre}
                    onChange={e => setForm({ ...form, nombre: e.target.value })}
                    required
                    style={inputStyle}
                    placeholder="Nombre del administrador"
                  />
                </div>

                <div style={{ marginBottom: '16px' }}>
                  <label style={labelStyle}>Correo electrónico</label>
                  <input
                    type="email"
                    value={form.correo}
                    onChange={e => setForm({ ...form, correo: e.target.value })}
                    required
                    style={inputStyle}
                    placeholder="correo@ejemplo.com"
                  />
                </div>

                <div style={{ marginBottom: '16px' }}>
                  <label style={labelStyle}>Contraseña</label>
                  <input
                    type="password"
                    value={form.password}
                    onChange={e => setForm({ ...form, password: e.target.value })}
                    required
                    minLength={8}
                    style={inputStyle}
                    placeholder="Mínimo 8 caracteres"
                  />
                </div>

                <div style={{ marginBottom: '24px' }}>
                  <label style={labelStyle}>Tipo de administrador</label>
                  <select
                    value={form.tipoAdmin}
                    onChange={e => setForm({ ...form, tipoAdmin: e.target.value })}
                    style={inputStyle}
                  >
                    <option value="comun">Común</option>
                    <option value="privilegiado">Privilegiado</option>
                  </select>
                </div>

                <button
                  type="submit"
                  disabled={loadingForm}
                  style={{
                    width: '100%',
                    padding: '12px',
                    backgroundColor: loadingForm ? '#888' : '#e94560',
                    color: 'white',
                    border: 'none',
                    borderRadius: '8px',
                    cursor: loadingForm ? 'not-allowed' : 'pointer',
                    fontSize: '15px',
                    fontWeight: 'bold'
                  }}
                >
                  {loadingForm ? 'Creando...' : '+ Crear Administrador'}
                </button>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default GestionAdmins;