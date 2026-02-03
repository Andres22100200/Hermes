const express = require('express');
const router = express.Router();

// Importar los controladores
const { 
  register, 
  login, 
  verifyOTP, 
  resendOTP 
} = require('../controllers/authController');

// Importar middleware
const { verificarToken } = require('../middlewares/authMiddleware');

// RUTAS DE AUTENTICACIÓN

/**
 * POST /api/auth/register
 * Registra un nuevo usuario y envía código OTP
 */
router.post('/register', register);

/**
 * POST /api/auth/login
 * Inicia sesión y devuelve un token JWT
 */
router.post('/login', login);

/**
 * POST /api/auth/verify-otp
 * Verifica el código OTP y activa la cuenta
 */
router.post('/verify-otp', verifyOTP);

/**
 * POST /api/auth/resend-otp
 * Reenvía el código OTP al correo del usuario
 */
router.post('/resend-otp', resendOTP);

// RUTA PROTEGIDA DE PRUEBA
// Esta ruta solo funciona si el usuario está autenticado
router.get('/perfil', verificarToken, (req, res) => {
  res.json({
    mensaje: 'Acceso autorizado a ruta protegida ✅',
    usuario: {
      id: req.usuario.id,
      nombre: req.usuario.nombre,
      apellido: req.usuario.apellido,
      correo: req.usuario.correo,
      fotoPerfil: req.usuario.fotoPerfil,
      biografia: req.usuario.biografia,
      generosPreferidos: req.usuario.generosPreferidos,
      promedioEstrellas_vendedor: req.usuario.promedioEstrellas_vendedor,
      promedioEstrellas_comprador: req.usuario.promedioEstrellas_comprador
    }
  });
});

module.exports = router;