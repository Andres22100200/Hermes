import axios from 'axios';

const BASE_URL = 'http://192.168.100.5:3000/api';

const api = axios.create({
  baseURL: BASE_URL
});

// Interceptor para agregar token automáticamente
api.interceptors.request.use(config => {
  const token = localStorage.getItem('adminToken');
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

export default api;