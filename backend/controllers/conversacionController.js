const Conversacion = require('../models/Conversacion');
const Mensaje = require('../models/Mensaje');
const User = require('../models/User');
const Publicacion = require('../models/Publicacion');

/**
 * INICIAR CONVERSACIÓN
 * POST /api/conversaciones
 */

const iniciarConversacion = async (req, res) => {
  try {
    // Verificar si el usuario está baneado
    const usuarioActual = await User.findByPk(req.usuario.id);
    if (!usuarioActual.activo) {
      return res.status(403).json({ error: 'Tu cuenta ha sido suspendida permanentemente' });
    }
    if (usuarioActual.suspendidoHasta && new Date(usuarioActual.suspendidoHasta) > new Date()) {
      const fechaFin = new Date(usuarioActual.suspendidoHasta).toLocaleDateString('es-MX');
      return res.status(403).json({ error: `Tu cuenta está suspendida hasta el ${fechaFin}` });
    }

    const { publicacionId } = req.body;
    const compradorId = req.usuario.id;

    const publicacion = await Publicacion.findByPk(publicacionId);

    if (!publicacion) {
      return res.status(404).json({ error: 'Publicación no encontrada' });
    }

    if (publicacion.usuarioId === compradorId) {
      return res.status(400).json({ error: 'No puedes contactarte a ti mismo' });
    }

    const vendedorId = publicacion.usuarioId;

    let conversacion = await Conversacion.findOne({
      where: { publicacionId, compradorId, vendedorId }
    });

    if (conversacion && conversacion.eliminadaPorComprador) {
      conversacion.eliminadaPorComprador = false;
      await conversacion.save();
    }

    if (!conversacion) {
      conversacion = await Conversacion.create({
        publicacionId, compradorId, vendedorId
      });

      const mensajeSeguridad = `🔒 PROTOCOLO DE SEGURIDAD:
- Informa a alguien de confianza sobre tu ubicación
- Reúnete en lugares públicos y concurridos
- Horario permitido: 11:00 AM - 7:00 PM
- Verifica el libro antes de pagar

¡Buena suerte con tu transacción! 📚`;

      await Mensaje.create({
        conversacionId: conversacion.id,
        remitenteId: vendedorId,
        tipo: 'sistema',
        contenido: mensajeSeguridad
      });

      conversacion.ultimoMensaje = 'Mensaje de seguridad';
      conversacion.ultimoMensajeFecha = new Date();
      await conversacion.save();
    }

    const conversacionCompleta = await Conversacion.findByPk(conversacion.id, {
      include: [
        { model: Publicacion, as: 'publicacion', attributes: ['id', 'titulo', 'fotos', 'precio'] },
        { model: User, as: 'comprador', attributes: ['id', 'nombre', 'apellido', 'fotoPerfil'] },
        { model: User, as: 'vendedor', attributes: ['id', 'nombre', 'apellido', 'fotoPerfil'] }
      ]
    });

    res.status(201).json({
      mensaje: 'Conversación iniciada',
      conversacion: conversacionCompleta
    });

  } catch (error) {
    console.error('Error al iniciar conversación:', error);
    res.status(500).json({ error: 'Error al iniciar conversación' });
  }
};

/**
 * OBTENER MIS CONVERSACIONES
 * GET /api/conversaciones
 */
const obtenerMisConversaciones = async (req, res) => {
  try {
    const usuarioId = req.usuario.id;

    const conversaciones = await Conversacion.findAll({
      where: {
        [require('sequelize').Op.or]: [
          { 
            compradorId: usuarioId,
            eliminadaPorComprador: false
          },
          { 
            vendedorId: usuarioId,
            eliminadaPorVendedor: false
          }
        ]
      },
      include: [
        {
          model: Publicacion,
          as: 'publicacion',
          attributes: ['id', 'titulo', 'fotos', 'precio', 'estado']
        },
        {
          model: User,
          as: 'comprador',
          attributes: ['id', 'nombre', 'apellido', 'fotoPerfil']
        },
        {
          model: User,
          as: 'vendedor',
          attributes: ['id', 'nombre', 'apellido', 'fotoPerfil']
        }
      ],
      order: [['ultimoMensajeFecha', 'DESC']]
    });

    res.json({
      total: conversaciones.length,
      conversaciones
    });

  } catch (error) {
    console.error('Error al obtener conversaciones:', error);
    res.status(500).json({ error: 'Error al obtener conversaciones' });
  }
};

/**
 * OBTENER MENSAJES DE UNA CONVERSACIÓN
 * GET /api/conversaciones/:id/mensajes
 */
const obtenerMensajes = async (req, res) => {
  try {
    const { id } = req.params;
    const usuarioId = req.usuario.id;

    // Verificar que el usuario pertenece a esta conversación
    const conversacion = await Conversacion.findByPk(id);

    if (!conversacion) {
      return res.status(404).json({ error: 'Conversación no encontrada' });
    }

    if (conversacion.compradorId !== usuarioId && conversacion.vendedorId !== usuarioId) {
      return res.status(403).json({ error: 'No tienes acceso a esta conversación' });
    }

    // Obtener mensajes
    const mensajes = await Mensaje.findAll({
      where: { conversacionId: id },
      include: [{
        model: User,
        as: 'remitente',
        attributes: ['id', 'nombre', 'apellido', 'fotoPerfil']
      }],
      order: [['createdAt', 'ASC']]
    });

    // Marcar mensajes como leídos
    await Mensaje.update(
      { leido: true },
      {
        where: {
          conversacionId: id,
          remitenteId: { [require('sequelize').Op.ne]: usuarioId },
          leido: false
        }
      }
    );

    res.json({
      total: mensajes.length,
      mensajes
    });

  } catch (error) {
    console.error('Error al obtener mensajes:', error);
    res.status(500).json({ error: 'Error al obtener mensajes' });
  }
};

/**
 * ENVIAR MENSAJE
 * POST /api/mensajes
 */
const enviarMensaje = async (req, res) => {
  try {
    const { conversacionId, contenido, tipo = 'texto' } = req.body;
    const remitenteId = req.usuario.id;

    // Verificar que el usuario pertenece a la conversación
    const conversacion = await Conversacion.findByPk(conversacionId);

    if (!conversacion) {
      return res.status(404).json({ error: 'Conversación no encontrada' });
    }

    if (conversacion.compradorId !== remitenteId && conversacion.vendedorId !== remitenteId) {
      return res.status(403).json({ error: 'No tienes acceso a esta conversación' });
    }

    // Crear mensaje
    const mensaje = await Mensaje.create({
      conversacionId,
      remitenteId,
      tipo,
      contenido
    });

    // Actualizar último mensaje en conversación
    conversacion.ultimoMensaje = contenido.substring(0, 50);
    conversacion.ultimoMensajeFecha = new Date();
    await conversacion.save();

    // Cargar mensaje completo con remitente
    const mensajeCompleto = await Mensaje.findByPk(mensaje.id, {
      include: [{
        model: User,
        as: 'remitente',
        attributes: ['id', 'nombre', 'apellido', 'fotoPerfil']
      }]
    });

    res.status(201).json({
      mensaje: 'Mensaje enviado',
      data: mensajeCompleto
    });

  } catch (error) {
    console.error('Error al enviar mensaje:', error);
    res.status(500).json({ error: 'Error al enviar mensaje' });
  }
};

/**
 * ELIMINAR CONVERSACIÓN (UNILATERAL)
 * DELETE /api/conversaciones/:id
 */
const eliminarConversacion = async (req, res) => {
  try {
    const { id } = req.params;
    const usuarioId = req.usuario.id;

    const conversacion = await Conversacion.findByPk(id);

    if (!conversacion) {
      return res.status(404).json({ error: 'Conversación no encontrada' });
    }

    // Marcar como eliminada según el rol del usuario
    if (conversacion.compradorId === usuarioId) {
      conversacion.eliminadaPorComprador = true;
    } else if (conversacion.vendedorId === usuarioId) {
      conversacion.eliminadaPorVendedor = true;
    } else {
      return res.status(403).json({ error: 'No tienes acceso a esta conversación' });
    }

    await conversacion.save();

    // Si ambos la eliminaron, eliminar físicamente
    if (conversacion.eliminadaPorComprador && conversacion.eliminadaPorVendedor) {
      await conversacion.destroy();
    }

    res.json({ mensaje: 'Conversación eliminada' });

  } catch (error) {
    console.error('Error al eliminar conversación:', error);
    res.status(500).json({ error: 'Error al eliminar conversación' });
  }
};

/**
 * MARCAR TRANSACCIÓN COMO COMPLETADA
 * PUT /api/conversaciones/:id/completar
 */
const completarTransaccion = async (req, res) => {
  try {
    const { id } = req.params;
    const usuarioId = req.usuario.id;

    const conversacion = await Conversacion.findByPk(id);

    if (!conversacion) {
      return res.status(404).json({ error: 'Conversación no encontrada' });
    }

    // Solo el vendedor puede marcar como completada
    if (conversacion.vendedorId !== usuarioId) {
      return res.status(403).json({ error: 'Solo el vendedor puede completar la transacción' });
    }

    conversacion.completada = true;
    await conversacion.save();

    res.json({ mensaje: 'Transacción marcada como completada' });

  } catch (error) {
    console.error('Error al completar transacción:', error);
    res.status(500).json({ error: 'Error al completar transacción' });
  }
};

module.exports = {
  iniciarConversacion,
  obtenerMisConversaciones,
  obtenerMensajes,
  enviarMensaje,
  eliminarConversacion,
  completarTransaccion
};