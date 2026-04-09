const express = require('express');
const router = express.Router();

const {
  loginAdmin,
  obtenerPerfilAdmin,
  crearAdmin,
  listarAdmins,
  eliminarAdmin,
  obtenerPublicacionesReportadas,
  obtenerUsuariosReportados,
  eliminarPublicacionAdmin,
  cerrarReporte,
  banearUsuario,
  banearUsuarioPermanente,
  eliminarReporte
} = require('../controllers/adminController');

const { 
  verificarTokenAdmin, 
  verificarAdminPrivilegiado 
} = require('../middlewares/authMiddleware');

// RUTAS PÚBLICAS
router.post('/login', loginAdmin);

// RUTAS PROTEGIDAS
router.get('/perfil', verificarTokenAdmin, obtenerPerfilAdmin);

// Gestión de admins (solo privilegiados)
router.get('/listar', verificarTokenAdmin, verificarAdminPrivilegiado, listarAdmins);
router.post('/crear', verificarTokenAdmin, verificarAdminPrivilegiado, crearAdmin);
router.delete('/eliminar/:id', verificarTokenAdmin, verificarAdminPrivilegiado, eliminarAdmin);

// Reportes
router.get('/reportes/publicaciones', verificarTokenAdmin, obtenerPublicacionesReportadas);
router.get('/reportes/usuarios', verificarTokenAdmin, obtenerUsuariosReportados);
router.put('/reportes/:id/cerrar', verificarTokenAdmin, cerrarReporte);
router.delete('/reportes/:id', verificarTokenAdmin, eliminarReporte);

// Acciones sobre publicaciones
router.delete('/publicacion/:id', verificarTokenAdmin, eliminarPublicacionAdmin);

// Acciones sobre usuarios
router.put('/usuario/:id/banear', verificarTokenAdmin, banearUsuario);
router.put('/usuario/:id/banear-permanente', verificarTokenAdmin, verificarAdminPrivilegiado, banearUsuarioPermanente);

module.exports = router;