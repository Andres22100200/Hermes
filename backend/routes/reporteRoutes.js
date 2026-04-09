const express = require('express');
const router = express.Router();
const { verificarToken } = require('../middlewares/authMiddleware');
const {
  reportarPublicacion,
  reportarUsuario,
  obtenerReportesPublicacion,
  obtenerReportesUsuario
} = require('../controllers/reporteController');

router.use(verificarToken);

/**
 * POST /api/reportes/publicacion
 * Reportar una publicación
 */
router.post('/publicacion', reportarPublicacion);

/**
 * POST /api/reportes/usuario
 * Reportar un usuario
 */
router.post('/usuario', reportarUsuario);

/**
 * GET /api/reportes/publicacion/:id
 * Obtener reportes de una publicación (admin)
 */
router.get('/publicacion/:id', obtenerReportesPublicacion);

/**
 * GET /api/reportes/usuario/:id
 * Obtener reportes de un usuario (admin)
 */
router.get('/usuario/:id', obtenerReportesUsuario);

module.exports = router;