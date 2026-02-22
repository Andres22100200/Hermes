const User = require('../models/User');

/**
 * OBTENER PERFIL DEL USUARIO AUTENTICADO
 * GET /api/profile
 */
const obtenerPerfil = async (req, res) => {
  try {
    // req.usuario viene del middleware verificarToken
    const usuario = await User.findByPk(req.usuario.id, {
      attributes: { exclude: ['password', 'codigoOTP', 'otpExpiracion', 'tokenRecuperacion', 'tokenRecuperacionExpiracion'] }
    });
    
    if (!usuario) {
      return res.status(404).json({
        error: 'Usuario no encontrado'
      });
    }
    
    res.json({
      usuario: {
        id: usuario.id,
        nombre: usuario.nombre,
        apellido: usuario.apellido,
        correo: usuario.correo,
        numeroTelefonico: usuario.numeroTelefonico,
        fechaNacimiento: usuario.fechaNacimiento,
        sexo: usuario.sexo,
        fotoPerfil: usuario.fotoPerfil,
        biografia: usuario.biografia,
        generosPreferidos: usuario.generosPreferidos,
        promedioEstrellas_vendedor: usuario.promedioEstrellas_vendedor,
        totalValoraciones_vendedor: usuario.totalValoraciones_vendedor,
        promedioEstrellas_comprador: usuario.promedioEstrellas_comprador,
        totalValoraciones_comprador: usuario.totalValoraciones_comprador
      }
    });
    
  } catch (error) {
    console.error('Error al obtener perfil:', error);
    res.status(500).json({
      error: 'Error al obtener perfil',
      detalle: error.message
    });
  }
};

/**
 * ACTUALIZAR BIOGRAFÍA
 * PUT /api/profile/biografia
 */
const actualizarBiografia = async (req, res) => {
  try {
    const { biografia } = req.body;
    
    // Validar longitud (máximo 1000 caracteres según tu documento)
    if (biografia && biografia.length > 1000) {
      return res.status(400).json({
        error: 'La biografía no puede superar los 1000 caracteres'
      });
    }
    
    // Actualizar usuario
    const usuario = await User.findByPk(req.usuario.id);
    usuario.biografia = biografia || null;
    await usuario.save();
    
    res.json({
      mensaje: 'Biografía actualizada exitosamente',
      biografia: usuario.biografia
    });
    
  } catch (error) {
    console.error('Error al actualizar biografía:', error);
    res.status(500).json({
      error: 'Error al actualizar biografía',
      detalle: error.message
    });
  }
};

/**
 * ACTUALIZAR GÉNEROS PREFERIDOS
 * PUT /api/profile/generos
 */
const actualizarGeneros = async (req, res) => {
  try {
    const { generosPreferidos } = req.body;
    
    // Validar que sea un array
    if (!Array.isArray(generosPreferidos)) {
      return res.status(400).json({
        error: 'Los géneros deben ser un array'
      });
    }
    
    // Validar que tenga al menos un género
    if (generosPreferidos.length === 0) {
      return res.status(400).json({
        error: 'Debes seleccionar al menos un género'
      });
    }
    
    // Actualizar usuario
    const usuario = await User.findByPk(req.usuario.id);
    usuario.generosPreferidos = generosPreferidos;
    await usuario.save();
    
    res.json({
      mensaje: 'Géneros actualizados exitosamente',
      generosPreferidos: usuario.generosPreferidos
    });
    
  } catch (error) {
    console.error('Error al actualizar géneros:', error);
    res.status(500).json({
      error: 'Error al actualizar géneros',
      detalle: error.message
    });
  }
};

/**
 * ACTUALIZAR NOMBRE Y APELLIDO
 * PUT /api/profile/nombre
 */
const actualizarNombre = async (req, res) => {
  try {
    const { nombre, apellido } = req.body;
    
    // Validar campos
    if (!nombre || !apellido) {
      return res.status(400).json({
        error: 'Nombre y apellido son requeridos'
      });
    }
    
    // Actualizar usuario
    const usuario = await User.findByPk(req.usuario.id);
    usuario.nombre = nombre;
    usuario.apellido = apellido;
    await usuario.save();
    
    res.json({
      mensaje: 'Nombre actualizado exitosamente',
      nombre: usuario.nombre,
      apellido: usuario.apellido
    });
    
  } catch (error) {
    console.error('Error al actualizar nombre:', error);
    res.status(500).json({
      error: 'Error al actualizar nombre',
      detalle: error.message
    });
  }
};

/**
 * ACTUALIZAR FOTO DE PERFIL
 * POST /api/profile/foto
 */
const actualizarFotoPerfil = async (req, res) => {
  try {
    // Verificar que se subió un archivo
    if (!req.file) {
      return res.status(400).json({
        error: 'No se subió ninguna imagen'
      });
    }
    
    // Obtener usuario
    const usuario = await User.findByPk(req.usuario.id);
    
    // Si ya tenía foto, eliminar la anterior
    if (usuario.fotoPerfil) {
      const fs = require('fs');
      const path = require('path');
      const oldPath = path.join(__dirname, '..', 'uploads', 'profile-pictures', usuario.fotoPerfil);
      
      if (fs.existsSync(oldPath)) {
        fs.unlinkSync(oldPath);
      }
    }
    
    // Guardar nombre del nuevo archivo
    usuario.fotoPerfil = req.file.filename;
    await usuario.save();
    
    res.json({
      mensaje: 'Foto de perfil actualizada exitosamente',
      fotoPerfil: req.file.filename,
      url: `/uploads/profile-pictures/${req.file.filename}`
    });
    
  } catch (error) {
    console.error('Error al actualizar foto de perfil:', error);
    res.status(500).json({
      error: 'Error al actualizar foto de perfil',
      detalle: error.message
    });
  }
};

module.exports = {
  obtenerPerfil,
  actualizarBiografia,
  actualizarGeneros,
  actualizarNombre,
  actualizarFotoPerfil
};