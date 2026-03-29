const Favorito = require('../models/Favorito');
const Publicacion = require('../models/Publicacion');
const User = require('../models/User');

/**
 * AGREGAR A FAVORITOS
 * POST /api/favoritos
 */
const agregarFavorito = async (req, res) => {
  try {
    const { publicacionId } = req.body;
    const usuarioId = req.usuario.id;

    // Verificar que la publicación existe y está disponible
    const publicacion = await Publicacion.findByPk(publicacionId);
    if (!publicacion) {
      return res.status(404).json({ error: 'Publicación no encontrada' });
    }

    if (publicacion.estado !== 'Disponible') {
      return res.status(400).json({ error: 'Solo puedes guardar publicaciones disponibles' });
    }

    // No puedes guardar tus propias publicaciones
    if (publicacion.usuarioId === usuarioId) {
      return res.status(400).json({ error: 'No puedes guardar tus propias publicaciones' });
    }

    // Crear favorito (si ya existe, findOrCreate lo maneja)
    const [favorito, creado] = await Favorito.findOrCreate({
      where: { usuarioId, publicacionId }
    });

    if (!creado) {
      return res.status(400).json({ error: 'Ya está en tus favoritos' });
    }

    res.status(201).json({
      mensaje: 'Publicación guardada en favoritos',
      favorito
    });

  } catch (error) {
    console.error('Error al agregar favorito:', error);
    res.status(500).json({ error: 'Error al agregar favorito', detalle: error.message });
  }
};

/**
 * QUITAR DE FAVORITOS
 * DELETE /api/favoritos/:publicacionId
 */
const quitarFavorito = async (req, res) => {
  try {
    const { publicacionId } = req.params;
    const usuarioId = req.usuario.id;

    const favorito = await Favorito.findOne({
      where: { usuarioId, publicacionId }
    });

    if (!favorito) {
      return res.status(404).json({ error: 'No está en tus favoritos' });
    }

    await favorito.destroy();

    res.json({ mensaje: 'Publicación eliminada de favoritos' });

  } catch (error) {
    console.error('Error al quitar favorito:', error);
    res.status(500).json({ error: 'Error al quitar favorito', detalle: error.message });
  }
};

/**
 * OBTENER MIS FAVORITOS
 * GET /api/favoritos
 */
const obtenerFavoritos = async (req, res) => {
  try {
    const usuarioId = req.usuario.id;

    const favoritos = await Favorito.findAll({
      where: { usuarioId },
      include: [{
        model: Publicacion,
        as: 'publicacion',
        include: [{
          model: User,
          as: 'vendedor',
          attributes: ['id', 'nombre', 'apellido', 'fotoPerfil', 'promedioEstrellas_vendedor']
        }]
      }],
      order: [['createdAt', 'DESC']]
    });

    res.json({
      total: favoritos.length,
      favoritos
    });

  } catch (error) {
    console.error('Error al obtener favoritos:', error);
    res.status(500).json({ error: 'Error al obtener favoritos', detalle: error.message });
  }
};

/**
 * VERIFICAR SI ES FAVORITO
 * GET /api/favoritos/check/:publicacionId
 */
const verificarFavorito = async (req, res) => {
  try {
    const { publicacionId } = req.params;
    const usuarioId = req.usuario.id;

    const favorito = await Favorito.findOne({
      where: { usuarioId, publicacionId }
    });

    res.json({ esFavorito: !!favorito });

  } catch (error) {
    console.error('Error al verificar favorito:', error);
    res.status(500).json({ error: 'Error al verificar favorito', detalle: error.message });
  }
};

module.exports = {
  agregarFavorito,
  quitarFavorito,
  obtenerFavoritos,
  verificarFavorito
};