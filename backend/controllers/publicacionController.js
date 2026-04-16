const Publicacion = require('../models/Publicacion');
const User = require('../models/User');
const puntosEncuentro = require('../data/puntosEncuentro');

// Función helper para normalizar texto (quitar acentos)
const quitarAcentos = (texto) => {
  return texto.normalize("NFD").replace(/[\u0300-\u036f]/g, "");
};

/**
 * CREAR PUBLICACIÓN
 * POST /api/publicaciones
 */
const crearPublicacion = async (req, res) => {
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

    console.log('Body recibido:', req.body);
    console.log('Files recibidos:', req.files);
    const {
      titulo, autor, editorial, yearPublicacion, isbn,
      generos, estadoLibro, descripcion, precio, puntoEncuentro
    } = req.body;

    let generosArray = generos;
    if (typeof generos === 'string') {
      generosArray = [generos];
    } else if (Array.isArray(generos)) {
      generosArray = generos;
    } else {
      generosArray = [];
    }

    if (!titulo || !autor || !generosArray || generosArray.length === 0 || !estadoLibro || !precio || !puntoEncuentro) {
      return res.status(400).json({
        error: 'Campos requeridos: título, autor, géneros, estado, precio, punto de encuentro'
      });
    }

    if (generosArray.length > 3) {
      return res.status(400).json({ error: 'Debes seleccionar entre 1 y 3 géneros' });
    }

    if (isNaN(precio) || precio <= 0) {
      return res.status(400).json({ error: 'El precio debe ser mayor a 0' });
    }

    const estadosPermitidos = ['Nuevo', 'Como nuevo', 'Muy bueno', 'Bueno', 'Aceptable'];
    if (!estadosPermitidos.includes(estadoLibro)) {
      return res.status(400).json({ error: 'Estado de libro inválido' });
    }

    let fotos = [];
    if (req.files && req.files.length > 0) {
      fotos = req.files.map(file => file.filename);
    } else {
      return res.status(400).json({ error: 'Debes subir al menos 1 foto del libro (máximo 5)' });
    }

    if (fotos.length > 5) {
      return res.status(400).json({ error: 'Máximo 5 fotos permitidas' });
    }

    const generosNormalizados = generosArray.map(g => quitarAcentos(g));

    const publicacion = await Publicacion.create({
      usuarioId: req.usuario.id,
      titulo, autor, editorial, yearPublicacion, isbn,
      generos: generosNormalizados, estadoLibro, descripcion,
      precio, fotos, puntoEncuentro, estado: 'Disponible'
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
    res.status(500).json({ error: 'Error al crear publicación', detalle: error.message });
  }
};

/**
 * OBTENER MIS PUBLICACIONES
 * GET /api/publicaciones/user/mis-publicaciones
 */
const obtenerMisPublicaciones = async (req, res) => {
  const { Op } = require('sequelize');
  try {
    const publicaciones = await Publicacion.findAll({
      where: {
        usuarioId: req.usuario.id,
        estado: { [Op.ne]: 'Eliminado' } //
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
 * OBTENER TODAS LAS PUBLICACIONES DISPONIBLES CON FILTROS Y BÚSQUEDA
 * GET /api/publicaciones
 */
const obtenerPublicaciones = async (req, res) => {
  try {
    const { 
      busqueda,           // Búsqueda por título o autor
      genero,             // Filtro por género
      zona,               // Filtro por zona (parte del punto de encuentro)
      precioMin,          // Precio mínimo
      precioMax,          // Precio máximo
      ordenar             // Ordenamiento: 'precio_asc', 'precio_desc', 'reciente', 'antiguos'
    } = req.query;

    const { Op } = require('sequelize');

    // Construir condiciones WHERE dinámicamente
    const whereConditions = { estado: 'Disponible' };

    // Búsqueda por título/autor (normalizar)
    if (busqueda) {
      const busquedaNormalizada = quitarAcentos(busqueda);
      whereConditions[Op.or] = [
        { titulo: { [Op.like]: `%${busquedaNormalizada}%` } },
        { autor: { [Op.like]: `%${busquedaNormalizada}%` } }
      ];
    }

// Filtro por género (aceptar múltiples géneros separados por coma)
if (genero) {
  const generosArray = genero.split(',').map(g => quitarAcentos(g.trim()));
  
  const { fn, col, literal } = require('sequelize');
  
  if (generosArray.length > 1) {
    whereConditions[Op.and] = whereConditions[Op.and] || [];
    whereConditions[Op.and].push(
      literal(`(${generosArray.map(g => 
        `JSON_SEARCH(LOWER(generos), 'one', LOWER('${g}')) IS NOT NULL`
      ).join(' OR ')})`)
    );
  } else {
    whereConditions[Op.and] = whereConditions[Op.and] || [];
    whereConditions[Op.and].push(
      literal(`JSON_SEARCH(LOWER(generos), 'one', LOWER('${generosArray[0]}')) IS NOT NULL`)
    );
  }
}

    // Filtro por zona (busca en puntoEncuentro)
    if (zona) {
      whereConditions.puntoEncuentro = { [Op.like]: `%${zona}%` };
    }

    // Filtro por rango de precio
    if (precioMin || precioMax) {
      whereConditions.precio = {};
      if (precioMin) whereConditions.precio[Op.gte] = precioMin;
      if (precioMax) whereConditions.precio[Op.lte] = precioMax;
    }

    // Determinar ordenamiento
    let order = [['createdAt', 'DESC']]; // Por defecto: más recientes
    
    if (ordenar) {
      switch (ordenar) {
        case 'precio_asc':
          order = [['precio', 'ASC']];
          break;
        case 'precio_desc':
          order = [['precio', 'DESC']];
          break;
        case 'reciente':
          order = [['createdAt', 'DESC']];
          break;
        case 'antiguos':
          order = [['createdAt', 'ASC']];
          break;
      }
    }



    // Consulta con filtros
    const publicaciones = await Publicacion.findAll({
      where: whereConditions,
      include: [{
        model: User,
        as: 'vendedor',
        attributes: ['id', 'nombre', 'apellido', 'fotoPerfil', 'promedioEstrellas_vendedor']
      }],
      order: order
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
    
    // Buscar coordenadas del punto de encuentro
const puntoEncontrado = puntosEncuentro.find(p => p.nombre === publicacion.puntoEncuentro);

const publicacionConCoordenadas = {
  ...publicacion.toJSON(),
  coordenadasPunto: puntoEncontrado ? puntoEncontrado.coordenadas : null
};

res.json({ publicacion: publicacionConCoordenadas });
    
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
      // Normalizar géneros al actualizar
      publicacion.generos = generos.map(g => quitarAcentos(g));
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

    // Si se subieron fotos nuevas, reemplazar las anteriores
if (req.files && req.files.length > 0) {
    publicacion.fotos = req.files.map(file => file.filename);
}
    
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
      return res.status(404).json({ error: 'Publicación no encontrada' });
    }

    if (publicacion.usuarioId !== req.usuario.id) {
      return res.status(403).json({ error: 'No tienes permiso para eliminar esta publicación' });
    }

    await publicacion.destroy();

    res.json({ mensaje: 'Publicación eliminada exitosamente' });

  } catch (error) {
    console.error('Error al eliminar publicación:', error);
    res.status(500).json({ error: 'Error al eliminar publicación', detalle: error.message });
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

/**
 * CAMBIAR ESTADO DE PUBLICACIÓN
 * PUT /api/publicaciones/:id/estado
 */
const cambiarEstadoPublicacion = async (req, res) => {
  try {
    const estadosPermitidos = ['Disponible', 'Reservado', 'Vendido', 'Eliminado', 'Inactivo'];
    const { id } = req.params;
    const { estado } = req.body;

    if (!estadosPermitidos.includes(estado)) {
      return res.status(400).json({ error: 'Estado inválido' });
    }

    const publicacion = await Publicacion.findByPk(id);

    if (!publicacion) {
      return res.status(404).json({ error: 'Publicación no encontrada' });
    }

    if (publicacion.usuarioId !== req.usuario.id) {
      return res.status(403).json({ error: 'No tienes permiso para modificar esta publicación' });
    }

    publicacion.estado = estado;
    await publicacion.save();

    res.json({
      mensaje: 'Estado actualizado exitosamente',
      estado: publicacion.estado
    });

  } catch (error) {
    console.error('Error al cambiar estado:', error);
    res.status(500).json({ error: 'Error al cambiar estado', detalle: error.message });
  }
};

module.exports = {
  crearPublicacion,
  obtenerMisPublicaciones,
  obtenerPublicaciones,
  obtenerPublicacion,
  actualizarPublicacion,
  eliminarPublicacion,
  obtenerPuntosEncuentro,
  cambiarEstadoPublicacion 
};