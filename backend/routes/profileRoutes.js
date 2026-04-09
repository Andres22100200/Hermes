const express = require('express');
const router = express.Router();

// Importar controladores
const {
  obtenerPerfil,
  actualizarBiografia,
  actualizarGeneros,
  actualizarNombre,
  actualizarFotoPerfil,
  obtenerPerfilPublico
} = require('../controllers/profileController');

// Importar middleware
const { verificarToken } = require('../middlewares/authMiddleware');

// Importar multer
const { uploadProfilePicture } = require('../config/multer');

const { actualizarCuenta } = require('../controllers/profileController');

// TODAS las rutas de perfil requieren autenticación
router.use(verificarToken);

/**
 * GET /api/profile
 * Obtener perfil del usuario autenticado
 */
router.get('/', obtenerPerfil);

/**
 * PUT /api/profile/biografia
 * Actualizar biografía
 */
router.put('/biografia', actualizarBiografia);

/**
 * PUT /api/profile/generos
 * Actualizar géneros preferidos
 */
router.put('/generos', actualizarGeneros);

/**
 * PUT /api/profile/nombre
 * Actualizar nombre y apellido
 */
router.put('/nombre', actualizarNombre);

/**
 * POST /api/profile/foto
 * Actualizar foto de perfil
 */
router.post('/foto', uploadProfilePicture.single('foto'), actualizarFotoPerfil);

/**
 * GET /api/profile/usuario/:usuarioId
 * Obtener perfil público de un usuario
 */
router.get('/usuario/:usuarioId', obtenerPerfilPublico);

router.get('/', verificarToken, obtenerPerfil);


router.put('/cuenta', actualizarCuenta);

module.exports = router;