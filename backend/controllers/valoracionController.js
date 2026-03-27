const Valoracion = require('../models/Valoracion');
const Conversacion = require('../models/Conversacion');
const Mensaje = require('../models/Mensaje');
const User = require('../models/User');
const { Op } = require('sequelize');

/**
 * VERIFICAR SI PUEDE VALORAR
 * GET /api/valoraciones/puede-valorar/:conversacionId
 */
const puedeValorar = async (req, res) => {
  try {
    const { conversacionId } = req.params;
    const usuarioId = req.usuario.id;

    // 1. Obtener la conversación
    const conversacion = await Conversacion.findByPk(conversacionId);

    if (!conversacion) {
      return res.status(404).json({ error: 'Conversación no encontrada' });
    }

    // 2. Verificar que el usuario pertenece a la conversación
    if (conversacion.compradorId !== usuarioId && conversacion.vendedorId !== usuarioId) {
      return res.status(403).json({ error: 'No tienes acceso a esta conversación' });
    }

    // 3. Determinar rol del usuario en esta conversación
    const rolEmisor = conversacion.compradorId === usuarioId ? 'comprador' : 'vendedor';
    const receptorId = rolEmisor === 'comprador' ? conversacion.vendedorId : conversacion.compradorId;

    // 4. Verificar si ya valoró en esta conversación
    const yaValoro = await Valoracion.findOne({
      where: {
        conversacionId,
        emisorId: usuarioId
      }
    });

    if (yaValoro) {
      return res.json({
        puede: false,
        motivo: 'Ya valoraste esta transacción'
      });
    }

    // 5. Contar mensajes de ambas partes
    const mensajesComprador = await Mensaje.count({
      where: {
        conversacionId,
        remitenteId: conversacion.compradorId,
        tipo: { [Op.ne]: 'sistema' }
      }
    });

    const mensajesVendedor = await Mensaje.count({
      where: {
        conversacionId,
        remitenteId: conversacion.vendedorId,
        tipo: { [Op.ne]: 'sistema' }
      }
    });

    const totalMensajes = mensajesComprador + mensajesVendedor;

    if (totalMensajes < 5) {
      return res.json({
        puede: false,
        motivo: 'Se necesitan al menos 5 mensajes para valorar',
        mensajesActuales: totalMensajes
      });
    }

    // 6. Puede valorar
    res.json({
      puede: true,
      rolEmisor,
      receptorId
    });

  } catch (error) {
    console.error('Error al verificar valoración:', error);
    res.status(500).json({ error: 'Error al verificar valoración', detalle: error.message });
  }
};

/**
 * CREAR VALORACIÓN
 * POST /api/valoraciones
 */
const crearValoracion = async (req, res) => {
  try {
    const { conversacionId, estrellas, etiquetas } = req.body;
    const emisorId = req.usuario.id;

    // 1. Validaciones básicas
    if (!conversacionId || !estrellas) {
      return res.status(400).json({ error: 'conversacionId y estrellas son requeridos' });
    }

    if (estrellas < 1 || estrellas > 5) {
      return res.status(400).json({ error: 'Las estrellas deben ser entre 1 y 5' });
    }

    // 2. Obtener conversación
    const conversacion = await Conversacion.findByPk(conversacionId);

    if (!conversacion) {
      return res.status(404).json({ error: 'Conversación no encontrada' });
    }

    // 3. Verificar que pertenece a la conversación
    if (conversacion.compradorId !== emisorId && conversacion.vendedorId !== emisorId) {
      return res.status(403).json({ error: 'No tienes acceso a esta conversación' });
    }

    // 4. Verificar que no haya valorado antes
    const yaValoro = await Valoracion.findOne({
      where: { conversacionId, emisorId }
    });

    if (yaValoro) {
      return res.status(400).json({ error: 'Ya valoraste esta transacción' });
    }

    // 5. Determinar roles
    const rolEmisor = conversacion.compradorId === emisorId ? 'comprador' : 'vendedor';
    const receptorId = rolEmisor === 'comprador' ? conversacion.vendedorId : conversacion.compradorId;

    // 6. Crear la valoración
    const valoracion = await Valoracion.create({
      conversacionId,
      emisorId,
      receptorId,
      rolEmisor,
      estrellas,
      etiquetas: etiquetas || []
    });

    // 7. Actualizar promedio del receptor
    const todasLasValoraciones = await Valoracion.findAll({
      where: { receptorId }
    });

    // Separar por rol del emisor para actualizar el promedio correcto
    const valoracionesComoVendedor = todasLasValoraciones.filter(v => v.rolEmisor === 'comprador');
    const valoracionesComoComprador = todasLasValoraciones.filter(v => v.rolEmisor === 'vendedor');

    const receptor = await User.findByPk(receptorId);

    if (valoracionesComoVendedor.length > 0) {
      const sumaVendedor = valoracionesComoVendedor.reduce((acc, v) => acc + v.estrellas, 0);
      receptor.promedioEstrellas_vendedor = (sumaVendedor / valoracionesComoVendedor.length).toFixed(1);
      receptor.totalValoraciones_vendedor = valoracionesComoVendedor.length;
    }

    if (valoracionesComoComprador.length > 0) {
      const sumaComprador = valoracionesComoComprador.reduce((acc, v) => acc + v.estrellas, 0);
      receptor.promedioEstrellas_comprador = (sumaComprador / valoracionesComoComprador.length).toFixed(1);
      receptor.totalValoraciones_comprador = valoracionesComoComprador.length;
    }

    await receptor.save();

    res.status(201).json({
      mensaje: 'Valoración enviada exitosamente',
      valoracion
    });

  } catch (error) {
    console.error('Error al crear valoración:', error);
    res.status(500).json({ error: 'Error al crear valoración', detalle: error.message });
  }
};

/**
 * OBTENER VALORACIONES DE UN USUARIO
 * GET /api/valoraciones/usuario/:usuarioId
 */
const obtenerValoracionesUsuario = async (req, res) => {
  try {
    const { usuarioId } = req.params;

    const valoraciones = await Valoracion.findAll({
      where: { receptorId: usuarioId },
      include: [{
        model: User,
        as: 'emisor',
        attributes: ['id', 'nombre', 'apellido', 'fotoPerfil']
      }],
      order: [['createdAt', 'DESC']]
    });

    res.json({
      total: valoraciones.length,
      valoraciones
    });

  } catch (error) {
    console.error('Error al obtener valoraciones:', error);
    res.status(500).json({ error: 'Error al obtener valoraciones', detalle: error.message });
  }
};

module.exports = {
  puedeValorar,
  crearValoracion,
  obtenerValoracionesUsuario
};