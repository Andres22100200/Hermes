const Publicacion = require('../models/Publicacion');
const User = require('../models/User');
const puntosEncuentro = require('../data/puntosEncuentro');

/**
 * CREAR PUBLICACIÓN
 * POST /api/publicaciones
 */
const crearPublicacion = async (req, res) => {
   try {
    // DEBUG: Ver qué está llegando
    console.log('Body recibido:', req.body);
    console.log('Files recibidos:', req.files);
    const {
      titulo,
      autor,
      editorial,
      yearPublicacion,
      isbn,
      generos,
      estadoLibro,
      descripcion,
      precio,
      puntoEncuentro
    } = req.body;
    
    // Parsear géneros PRIMERO (antes de validar)
    let generosArray = generos;
    if (typeof generos === 'string') {
      generosArray = [generos];
    } else if (Array.isArray(generos)) {
      generosArray = generos;
    } else {
      generosArray = [];
    }

    // Validaciones
    if (!titulo || !autor || !generosArray || generosArray.length === 0 || !estadoLibro || !precio || !puntoEncuentro) {
      return res.status(400).json({
        error: 'Campos requeridos: título, autor, géneros, estado, precio, punto de encuentro'
      });
    }
    
    // Validar géneros (máximo 3)
    if (generosArray.length > 3) {
      return res.status(400).json({
        error: 'Debes seleccionar entre 1 y 3 géneros'
      });
    }
    
    // Validar precio
    if (isNaN(precio) || precio <= 0) {
      return res.status(400).json({
        error: 'El precio debe ser mayor a 0'
      });
    }
    
    // Validar estado del libro
    const estadosPermitidos = ['Nuevo', 'Como nuevo', 'Muy bueno', 'Bueno', 'Aceptable'];
    if (!estadosPermitidos.includes(estadoLibro)) {
      return res.status(400).json({
        error: 'Estado de libro inválido'
      });
    }
    
    // Procesar fotos (si hay)
    let fotos = [];
    if (req.files && req.files.length > 0) {
      fotos = req.files.map(file => file.filename);
    }
    
    // Crear publicación
    const publicacion = await Publicacion.create({
      usuarioId: req.usuario.id,
      titulo,
      autor,
      editorial,
      yearPublicacion,
      isbn,
      generos: generosArray,
      estadoLibro,
      descripcion,
      precio,
      fotos,
      puntoEncuentro,
      estado: 'Disponible'
    });
    
    res.status(201).json({
      mensaje: 'Publicación creada exitosamente',
      publicacion: {
        id: publicacion.id,
        titulo: publicacion.titulo,
        precio: publicacion.precio,
        fotos: publicacion.fotos
      }
    });
    
  } catch (error) {
    console.error('Error al crear publicación:', error);
    res.status(500).json({
      error: 'Error al crear publicación',
      detalle: error.message
    });
  }
};

/**
 * OBTENER MIS PUBLICACIONES
 * GET /api/publicaciones/mis-publicaciones
 */
const obtenerMisPublicaciones = async (req, res) => {
  try {
    const publicaciones = await Publicacion.findAll({
      where: {
        usuarioId: req.usuario.id,
        estado: ['Disponible', 'Reservado', 'Vendido']
      },
      order: [['createdAt', 'DESC']]
    });
    
    res.json({
      total: publicaciones.length,
      publicaciones
    });
    
  } catch (error) {
    console.error('Error al obtener publicaciones:', error);
    res.status(500).json({
      error: 'Error al obtener publicaciones',
      detalle: error.message
    });
  }
};

/**
 * OBTENER TODAS LAS PUBLICACIONES DISPONIBLES
 * GET /api/publicaciones
 */
const obtenerPublicaciones = async (req, res) => {
  try {
    const { genero, busqueda } = req.query;
    
    let whereClause = {
      estado: 'Disponible'
    };
    
    // Filtro por género
    if (genero) {
      whereClause.generos = {
        [require('sequelize').Op.contains]: [genero]
      };
    }
    
    // Búsqueda por título o autor
    if (busqueda) {
      whereClause[require('sequelize').Op.or] = [
        { titulo: { [require('sequelize').Op.like]: `%${busqueda}%` } },
        { autor: { [require('sequelize').Op.like]: `%${busqueda}%` } }
      ];
    }
    
    const publicaciones = await Publicacion.findAll({
      where: whereClause,
      include: [{
        model: User,
        as: 'vendedor',
        attributes: ['id', 'nombre', 'apellido', 'fotoPerfil', 'promedioEstrellas_vendedor']
      }],
      order: [['createdAt', 'DESC']]
    });
    
    res.json({
      total: publicaciones.length,
      publicaciones
    });
    
  } catch (error) {
    console.error('Error al obtener publicaciones:', error);
    res.status(500).json({
      error: 'Error al obtener publicaciones',
      detalle: error.message
    });
  }
};

/**
 * OBTENER DETALLE DE PUBLICACIÓN
 * GET /api/publicaciones/:id
 */
const obtenerPublicacion = async (req, res) => {
  try {
    const { id } = req.params;
    
    const publicacion = await Publicacion.findByPk(id, {
      include: [{
        model: User,
        as: 'vendedor',
        attributes: ['id', 'nombre', 'apellido', 'fotoPerfil', 'biografia', 'promedioEstrellas_vendedor', 'totalValoraciones_vendedor']
      }]
    });
    
    if (!publicacion) {
      return res.status(404).json({
        error: 'Publicación no encontrada'
      });
    }
    
    res.json({ publicacion });
    
  } catch (error) {
    console.error('Error al obtener publicación:', error);
    res.status(500).json({
      error: 'Error al obtener publicación',
      detalle: error.message
    });
  }
};

/**
 * ACTUALIZAR PUBLICACIÓN
 * PUT /api/publicaciones/:id
 */
const actualizarPublicacion = async (req, res) => {
  try {
    const { id } = req.params;
    const {
      titulo,
      autor,
      editorial,
      yearPublicacion,
      isbn,
      generos,
      estadoLibro,
      descripcion,
      precio,
      puntoEncuentro
    } = req.body;
    
    const publicacion = await Publicacion.findByPk(id);
    
    if (!publicacion) {
      return res.status(404).json({
        error: 'Publicación no encontrada'
      });
    }
    
    if (publicacion.usuarioId !== req.usuario.id) {
      return res.status(403).json({
        error: 'No tienes permiso para editar esta publicación'
      });
    }
    
    if (titulo) publicacion.titulo = titulo;
    if (autor) publicacion.autor = autor;
    if (editorial !== undefined) publicacion.editorial = editorial;
    if (yearPublicacion) publicacion.yearPublicacion = yearPublicacion;
    if (isbn !== undefined) publicacion.isbn = isbn;
    if (generos) {
      if (!Array.isArray(generos) || generos.length === 0 || generos.length > 3) {
        return res.status(400).json({
          error: 'Debes seleccionar entre 1 y 3 géneros'
        });
      }
      publicacion.generos = generos;
    }
    if (estadoLibro) publicacion.estadoLibro = estadoLibro;
    if (descripcion !== undefined) publicacion.descripcion = descripcion;
    if (precio) {
      if (isNaN(precio) || precio <= 0) {
        return res.status(400).json({
          error: 'El precio debe ser mayor a 0'
        });
      }
      publicacion.precio = precio;
    }
    if (puntoEncuentro) publicacion.puntoEncuentro = puntoEncuentro;
    
    await publicacion.save();
    
    res.json({
      mensaje: 'Publicación actualizada exitosamente',
      publicacion
    });
    
  } catch (error) {
    console.error('Error al actualizar publicación:', error);
    res.status(500).json({
      error: 'Error al actualizar publicación',
      detalle: error.message
    });
  }
};

/**
 * ELIMINAR PUBLICACIÓN (lógico)
 * DELETE /api/publicaciones/:id
 */
const eliminarPublicacion = async (req, res) => {
  try {
    const { id } = req.params;
    
    const publicacion = await Publicacion.findByPk(id);
    
    if (!publicacion) {
      return res.status(404).json({
        error: 'Publicación no encontrada'
      });
    }
    
    if (publicacion.usuarioId !== req.usuario.id) {
      return res.status(403).json({
        error: 'No tienes permiso para eliminar esta publicación'
      });
    }
    
    publicacion.estado = 'Eliminado';
    await publicacion.save();
    
    res.json({
      mensaje: 'Publicación eliminada exitosamente'
    });
    
  } catch (error) {
    console.error('Error al eliminar publicación:', error);
    res.status(500).json({
      error: 'Error al eliminar publicación',
      detalle: error.message
    });
  }
};

/**
 * OBTENER CATÁLOGO DE PUNTOS DE ENCUENTRO
 * GET /api/publicaciones/puntos-encuentro
 */
const obtenerPuntosEncuentro = (req, res) => {
  try {
    res.json({
      total: puntosEncuentro.length,
      puntos: puntosEncuentro
    });
  } catch (error) {
    console.error('Error al obtener puntos de encuentro:', error);
    res.status(500).json({
      error: 'Error al obtener puntos de encuentro'
    });
  }
};

module.exports = {
  crearPublicacion,
  obtenerMisPublicaciones,
  obtenerPublicaciones,
  obtenerPublicacion,
  actualizarPublicacion,
  eliminarPublicacion,
  obtenerPuntosEncuentro
};