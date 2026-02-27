const express = require('express');
const router = express.Router();

// Importar controladores
const {
  crearPublicacion,
  obtenerMisPublicaciones,
  obtenerPublicaciones,
  obtenerPublicacion,
  actualizarPublicacion,
  eliminarPublicacion,
  obtenerPuntosEncuentro
} = require('../controllers/publicacionController');

// Importar middleware
const { verificarToken } = require('../middlewares/authMiddleware');

// Importar multer para fotos de libros
const { uploadBookPictures } = require('../config/multer');

/**
 * GET /api/publicaciones/puntos-encuentro
 * Obtener catálogo de puntos de encuentro (público)
 */
router.get('/puntos-encuentro', obtenerPuntosEncuentro);

/**
 * GET /api/publicaciones
 * Obtener todas las publicaciones disponibles (público)
 */
router.get('/', obtenerPublicaciones);

/**
 * GET /api/publicaciones/:id
 * Obtener detalle de una publicación (público)
 */
router.get('/:id', obtenerPublicacion);

// RUTAS PROTEGIDAS (requieren autenticación)

/**
 * POST /api/publicaciones
 * Crear nueva publicación
 */
router.post('/', verificarToken, uploadBookPictures.array('fotos', 5), crearPublicacion);

/**
 * GET /api/publicaciones/mis-publicaciones
 * Obtener publicaciones del usuario autenticado
 */
router.get('/user/mis-publicaciones', verificarToken, obtenerMisPublicaciones);

/**
 * PUT /api/publicaciones/:id
 * Actualizar publicación
 */
router.put('/:id', verificarToken, actualizarPublicacion);

/**
 * DELETE /api/publicaciones/:id
 * Eliminar publicación (lógico)
 */
router.delete('/:id', verificarToken, eliminarPublicacion);

module.exports = router;