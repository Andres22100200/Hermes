const express = require('express');
const router = express.Router();
const { verificarToken } = require('../middlewares/authMiddleware');
const {
  puedeValorar,
  crearValoracion,
  obtenerValoracionesUsuario
} = require('../controllers/valoracionController');

// Todas las rutas requieren autenticación
router.use(verificarToken);

/**
 * GET /api/valoraciones/puede-valorar/:conversacionId
 * Verificar si el usuario puede valorar esta conversación
 */
router.get('/puede-valorar/:conversacionId', puedeValorar);

/**
 * POST /api/valoraciones
 * Crear una valoración
 */
router.post('/', crearValoracion);

/**
 * GET /api/valoraciones/usuario/:usuarioId
 * Obtener valoraciones de un usuario
 */
router.get('/usuario/:usuarioId', obtenerValoracionesUsuario);

module.exports = router;