const express = require('express');
const router = express.Router();
const { verificarToken } = require('../middlewares/authMiddleware');
const {
  agregarFavorito,
  quitarFavorito,
  obtenerFavoritos,
  verificarFavorito
} = require('../controllers/favoritoController');

router.use(verificarToken);

/**
 * GET /api/favoritos
 * Obtener mis favoritos
 */
router.get('/', obtenerFavoritos);

/**
 * GET /api/favoritos/check/:publicacionId
 * Verificar si una publicación es favorita
 */
router.get('/check/:publicacionId', verificarFavorito);

/**
 * POST /api/favoritos
 * Agregar a favoritos
 */
router.post('/', agregarFavorito);

/**
 * DELETE /api/favoritos/:publicacionId
 * Quitar de favoritos
 */
router.delete('/:publicacionId', quitarFavorito);

module.exports = router;