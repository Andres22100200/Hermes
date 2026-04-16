import axios from 'axios';

export const BASE_URL = 'http://192.168.100.11:3000';

const api = axios.create({
  baseURL: `${BASE_URL}/api`
});

// Interceptor para agregar token automáticamente
api.interceptors.request.use(config => {
  const token = sessionStorage.getItem('adminToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// ========== AUTH ==========
export const loginAdmin = (correo, password) =>
  api.post('/admin/login', { correo, password });

export const obtenerPerfil = () =>
  api.get('/admin/perfil');

// ========== REPORTES ==========
export const obtenerPublicacionesReportadas = () =>
  api.get('/admin/reportes/publicaciones');

export const obtenerUsuariosReportados = () =>
  api.get('/admin/reportes/usuarios');

export const cerrarReporte = (id) =>
  api.put(`/admin/reportes/${id}/cerrar`);

export const eliminarReporte = (id) =>
  api.delete(`/admin/reportes/${id}`);

// ========== PUBLICACIONES ==========
export const eliminarPublicacion = (id) =>
  api.delete(`/admin/publicacion/${id}`);

// ========== USUARIOS ==========
export const banearUsuario = (id, dias, motivo) =>
  api.put(`/admin/usuario/${id}/banear`, { dias, motivo });

export const banearUsuarioPermanente = (id, motivo) =>
  api.put(`/admin/usuario/${id}/banear-permanente`, { motivo });

// ========== ADMINS ==========
export const listarAdmins = () =>
  api.get('/admin/listar');

export const crearAdmin = (datos) =>
  api.post('/admin/crear', datos);

export const eliminarAdmin = (id) =>
  api.delete(`/admin/eliminar/${id}`);

export const revocarSuspension = (id) =>
  api.put(`/admin/usuario/${id}/revocar-suspension`);

export const actualizarPasswordAdmin = (id, nuevaPassword) =>
  api.put(`/admin/cambiar-password/${id}`, { nuevaPassword });

export default api;