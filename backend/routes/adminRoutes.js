const express = require('express');
const router = express.Router();

// Importar controladores
const {
  loginAdmin,
  obtenerPerfilAdmin,
  crearAdmin,
  listarAdmins,
  eliminarAdmin
} = require('../controllers/adminController');

// Importar middlewares
const { 
  verificarTokenAdmin, 
  verificarAdminPrivilegiado 
} = require('../middlewares/authMiddleware');

// RUTAS PÚBLICAS (no requieren autenticación)

/**
 * POST /api/admin/login
 * Login de administrador
 */
router.post('/login', loginAdmin);

// RUTAS PROTEGIDAS (requieren token de admin)

/**
 * GET /api/admin/perfil
 * Obtener perfil del admin autenticado
 */
router.get('/perfil', verificarTokenAdmin, obtenerPerfilAdmin);

/**
 * GET /api/admin/listar
 * Listar todos los administradores (Solo admins privilegiados)
 */
router.get('/listar', verificarTokenAdmin, verificarAdminPrivilegiado, listarAdmins);

/**
 * POST /api/admin/crear
 * Crear nuevo administrador (Solo admins privilegiados)
 */
router.post('/crear', verificarTokenAdmin, verificarAdminPrivilegiado, crearAdmin);

/**
 * DELETE /api/admin/eliminar/:id
 * Eliminar administrador común (Solo admins privilegiados)
 */
router.delete('/eliminar/:id', verificarTokenAdmin, verificarAdminPrivilegiado, eliminarAdmin);

module.exports = router;