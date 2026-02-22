const express = require('express');
const router = express.Router();

// Importar controladores
const {
  obtenerPerfil,
  actualizarBiografia,
  actualizarGeneros,
  actualizarNombre,
  actualizarFotoPerfil
} = require('../controllers/profileController');

// Importar middleware
const { verificarToken } = require('../middlewares/authMiddleware');

// Importar multer
const upload = require('../config/multer');

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
router.post('/foto', upload.single('foto'), actualizarFotoPerfil);

module.exports = router;