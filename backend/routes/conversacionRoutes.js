const express = require('express');
const router = express.Router();
const { verificarToken } = require('../middlewares/authMiddleware');
const {
  iniciarConversacion,
  obtenerMisConversaciones,
  obtenerMensajes,
  enviarMensaje,
  eliminarConversacion,
  completarTransaccion
} = require('../controllers/conversacionController');

// Todas las rutas requieren autenticación
router.use(verificarToken);

// Iniciar conversación
router.post('/', iniciarConversacion);

// Obtener mis conversaciones
router.get('/', obtenerMisConversaciones);

// Obtener mensajes de una conversación
router.get('/:id/mensajes', obtenerMensajes);

// Enviar mensaje
router.post('/mensajes', enviarMensaje);

// Eliminar conversación (unilateral)
router.delete('/:id', eliminarConversacion);

// Marcar transacción como completada
router.put('/:id/completar', completarTransaccion);

module.exports = router;