const jwt = require('jsonwebtoken');
const Admin = require('../models/Admin');
const Reporte = require('../models/Reporte');
const Publicacion = require('../models/Publicacion');
const User = require('../models/User');
const { Op } = require('sequelize');

const DIAS_VIGENCIA = 4;
const getVigenciaFecha = () => {
  const fecha = new Date();
  fecha.setDate(fecha.getDate() - DIAS_VIGENCIA);
  return fecha;
};

/**
 * LOGIN DE ADMINISTRADOR
 * POST /api/admin/login
 */
const loginAdmin = async (req, res) => {
  try {
    const { correo, password } = req.body;

    // 1. Validación de campos
    if (!correo || !password) {
      return res.status(400).json({
        error: 'Correo y contraseña son requeridos'
      });
    }

    // 2. Buscar administrador
    const admin = await Admin.findOne({ where: { correo } });
    
    if (!admin) {
      return res.status(401).json({
        error: 'Correo o contraseña incorrectos'
      });
    }

    // 3. Verificar que la cuenta esté activa
    if (!admin.activo) {
      return res.status(403).json({
        error: 'Tu cuenta de administrador está desactivada'
      });
    }

    // 4. Verificar contraseña
    const passwordValido = await admin.compararPassword(password);
    
    if (!passwordValido) {
      return res.status(401).json({
        error: 'Correo o contraseña incorrectos'
      });
    }

    // 5. Generar token JWT
    const token = jwt.sign(
      { 
        id: admin.id,
        correo: admin.correo,
        tipo: 'admin',
        tipoAdmin: admin.tipoAdmin,
        esSuperAdmin: admin.esSuperAdmin
      },
      process.env.JWT_SECRET,
      { expiresIn: '7d' } // Token válido por 7 días para admins
    );

    // 6. Respuesta exitosa
    res.json({
      mensaje: 'Login de administrador exitoso',
      token,
      admin: {
        id: admin.id,
        nombre: admin.nombre,
        correo: admin.correo,
        tipoAdmin: admin.tipoAdmin,
        esSuperAdmin: admin.esSuperAdmin
      }
    });

  } catch (error) {
    console.error('Error en login admin:', error);
    res.status(500).json({
      error: 'Error al iniciar sesión',
      detalle: error.message
    });
  }
};

/**
 * OBTENER PERFIL DE ADMINISTRADOR
 * GET /api/admin/perfil
 */
const obtenerPerfilAdmin = async (req, res) => {
  try {
    // req.admin viene del middleware verificarTokenAdmin
    res.json({
      admin: {
        id: req.admin.id,
        nombre: req.admin.nombre,
        correo: req.admin.correo,
        tipoAdmin: req.admin.tipoAdmin,
        esSuperAdmin: req.admin.esSuperAdmin,
        activo: req.admin.activo
      }
    });
  } catch (error) {
    console.error('Error al obtener perfil admin:', error);
    res.status(500).json({
      error: 'Error al obtener perfil',
      detalle: error.message
    });
  }
};

/**
 * CREAR NUEVO ADMINISTRADOR (Solo admins privilegiados)
 * POST /api/admin/crear
 */
const crearAdmin = async (req, res) => {
  try {
    const { nombre, correo, password, tipoAdmin } = req.body;

    // 1. Validación de campos
    if (!nombre || !correo || !password || !tipoAdmin) {
      return res.status(400).json({
        error: 'Todos los campos son obligatorios'
      });
    }

    // 2. Validar tipo de admin
    if (!['privilegiado', 'comun'].includes(tipoAdmin)) {
      return res.status(400).json({
        error: 'Tipo de administrador inválido. Debe ser "privilegiado" o "comun"'
      });
    }

    // 3. Verificar que el correo no exista
    const adminExistente = await Admin.findOne({ where: { correo } });
    if (adminExistente) {
      return res.status(400).json({
        error: 'Este correo ya está registrado'
      });
    }

    // 4. Validación: contraseña mínimo 8 caracteres
    if (password.length < 8) {
      return res.status(400).json({
        error: 'La contraseña debe tener al menos 8 caracteres'
      });
    }

    // 5. Crear el administrador
    const nuevoAdmin = await Admin.create({
      nombre,
      correo,
      password, // Se encripta automáticamente
      tipoAdmin,
      esSuperAdmin: false,
      activo: true,
      creadoPorAdminId: req.admin.id // El admin que está creando este nuevo admin
    });

    // 6. Respuesta exitosa
    res.status(201).json({
      mensaje: 'Administrador creado exitosamente',
      admin: {
        id: nuevoAdmin.id,
        nombre: nuevoAdmin.nombre,
        correo: nuevoAdmin.correo,
        tipoAdmin: nuevoAdmin.tipoAdmin
      }
    });

  } catch (error) {
    console.error('Error al crear admin:', error);
    res.status(500).json({
      error: 'Error al crear administrador',
      detalle: error.message
    });
  }
};

/**
 * LISTAR TODOS LOS ADMINISTRADORES (Solo admins privilegiados)
 * GET /api/admin/listar
 */
const listarAdmins = async (req, res) => {
  try {
    const admins = await Admin.findAll({
      attributes: ['id', 'nombre', 'correo', 'tipoAdmin', 'esSuperAdmin', 'activo', 'createdAt'],
      order: [['createdAt', 'DESC']]
    });

    res.json({
      total: admins.length,
      administradores: admins
    });

  } catch (error) {
    console.error('Error al listar admins:', error);
    res.status(500).json({
      error: 'Error al listar administradores',
      detalle: error.message
    });
  }
};

/**
 * ELIMINAR ADMINISTRADOR (Solo admins privilegiados)
 * DELETE /api/admin/eliminar/:id
 */
const eliminarAdmin = async (req, res) => {
  try {
    const { id } = req.params;

    // 1. Buscar el admin a eliminar
    const adminAEliminar = await Admin.findByPk(id);
    
    if (!adminAEliminar) {
      return res.status(404).json({
        error: 'Administrador no encontrado'
      });
    }

    // 2. No se puede eliminar al SuperAdmin
    if (adminAEliminar.esSuperAdmin) {
      return res.status(403).json({
        error: 'No se puede eliminar al SuperAdmin'
      });
    }

    // 3. No se puede eliminar a administradores privilegiados
    if (adminAEliminar.tipoAdmin === 'privilegiado') {
      return res.status(403).json({
        error: 'No se pueden eliminar administradores privilegiados'
      });
    }

    // 4. Eliminar el administrador
    await adminAEliminar.destroy();

    // 5. Respuesta exitosa
    res.json({
      mensaje: 'Administrador eliminado exitosamente',
      adminEliminado: {
        id: adminAEliminar.id,
        nombre: adminAEliminar.nombre,
        correo: adminAEliminar.correo
      }
    });

  } catch (error) {
    console.error('Error al eliminar admin:', error);
    res.status(500).json({
      error: 'Error al eliminar administrador',
      detalle: error.message
    });
  }
};


/**
 * OBTENER PUBLICACIONES REPORTADAS
 * GET /api/admin/reportes/publicaciones
 */
const obtenerPublicacionesReportadas = async (req, res) => {
  try {
    const publicaciones = await Publicacion.findAll({
      include: [{
        model: Reporte,
        as: 'reportes',
        where: { 
          createdAt: { [Op.gte]: getVigenciaFecha() },
          procesado: false
        },
        required: true
      }, {
        model: User,
        as: 'vendedor',
        attributes: ['id', 'nombre', 'apellido', 'fotoPerfil']
      }]
    });

    const resultado = publicaciones.map(pub => ({
      ...pub.toJSON(),
      totalReportes: pub.reportes.length
    }));

    resultado.sort((a, b) => b.totalReportes - a.totalReportes);

    res.json({ total: resultado.length, publicaciones: resultado });

  } catch (error) {
    console.error('Error:', error);
    res.status(500).json({ error: 'Error al obtener publicaciones reportadas', detalle: error.message });
  }
};

/**
 * OBTENER USUARIOS REPORTADOS
 * GET /api/admin/reportes/usuarios
 */
const obtenerUsuariosReportados = async (req, res) => {
  try {
    const usuarios = await User.findAll({
      include: [{
        model: Reporte,
        as: 'reportesRecibidos',
        where: {
          tipo: 'usuario',
          procesado: false,
          createdAt: { [Op.gte]: getVigenciaFecha() }
        },
        required: true
      }]
    });

    const resultado = usuarios.map(u => ({
      ...u.toJSON(),
      totalReportes: u.reportesRecibidos.length
    }));

    resultado.sort((a, b) => b.totalReportes - a.totalReportes);

    res.json({ total: resultado.length, usuarios: resultado });

  } catch (error) {
    console.error('Error:', error);
    res.status(500).json({ error: 'Error al obtener usuarios reportados', detalle: error.message });
  }
};

/**
 * ELIMINAR PUBLICACIÓN (acción admin)
 * DELETE /api/admin/publicacion/:id
 */
const eliminarPublicacionAdmin = async (req, res) => {
  try {
    const { id } = req.params;
    const publicacion = await Publicacion.findByPk(id);

    if (!publicacion) {
      return res.status(404).json({ error: 'Publicación no encontrada' });
    }

    await publicacion.destroy();

    res.json({ mensaje: 'Publicación eliminada exitosamente' });

  } catch (error) {
    res.status(500).json({ error: 'Error al eliminar publicación', detalle: error.message });
  }
};

/**
 * CERRAR REPORTE SIN ACCIÓN
 * PUT /api/admin/reportes/:id/cerrar
 */
const cerrarReporte = async (req, res) => {
  try {
    const { id } = req.params;
    const reporte = await Reporte.findByPk(id);

    if (!reporte) {
      return res.status(404).json({ error: 'Reporte no encontrado' });
    }

    reporte.procesado = true;
    await reporte.save();

    res.json({ mensaje: 'Reporte cerrado sin acción' });

  } catch (error) {
    res.status(500).json({ error: 'Error al cerrar reporte', detalle: error.message });
  }
};

/**
 * BANEAR USUARIO TEMPORALMENTE
 * PUT /api/admin/usuario/:id/banear
 */
const banearUsuario = async (req, res) => {
  try {
    const { id } = req.params;
    const { dias, motivo } = req.body;

    if (!dias || ![2, 7].includes(parseInt(dias))) {
      return res.status(400).json({ error: 'Los días de ban deben ser 2 o 7' });
    }

    const usuario = await User.findByPk(id);
    if (!usuario) {
      return res.status(404).json({ error: 'Usuario no encontrado' });
    }

    const suspension = new Date();
    suspension.setDate(suspension.getDate() + parseInt(dias));
    usuario.suspendidoHasta = suspension;
    usuario.motivoSuspension = motivo || `Ban temporal de ${dias} días por admin`;
    await usuario.save();

    res.json({ mensaje: `Usuario baneado por ${dias} días` });

  } catch (error) {
    res.status(500).json({ error: 'Error al banear usuario', detalle: error.message });
  }
};

/**
 * BANEAR USUARIO PERMANENTEMENTE
 * PUT /api/admin/usuario/:id/banear-permanente
 */
const banearUsuarioPermanente = async (req, res) => {
  try {
    const { id } = req.params;

    const usuario = await User.findByPk(id);
    if (!usuario) {
      return res.status(404).json({ error: 'Usuario no encontrado' });
    }

    await usuario.destroy();

    res.json({ mensaje: 'Cuenta eliminada permanentemente' });

  } catch (error) {
    res.status(500).json({ error: 'Error al eliminar cuenta', detalle: error.message });
  }
};

/**
 * ELIMINAR REPORTE INDIVIDUAL
 * DELETE /api/admin/reportes/:id
 */
const eliminarReporte = async (req, res) => {
  try {
    const { id } = req.params;
    const reporte = await Reporte.findByPk(id);

    if (!reporte) {
      return res.status(404).json({ error: 'Reporte no encontrado' });
    }

    await reporte.destroy();

    res.json({ mensaje: 'Reporte eliminado exitosamente' });

  } catch (error) {
    res.status(500).json({ error: 'Error al eliminar reporte', detalle: error.message });
  }
};

/**
 * REVOCAR SUSPENSIÓN DE USUARIO
 * PUT /api/admin/usuario/:id/revocar-suspension
 */
const revocarSuspension = async (req, res) => {
  try {
    const { id } = req.params;

    const usuario = await User.findByPk(id);
    if (!usuario) {
      return res.status(404).json({ error: 'Usuario no encontrado' });
    }

    usuario.suspendidoHasta = null;
    usuario.motivoSuspension = null;
    await usuario.save();

    res.json({ mensaje: 'Suspensión revocada exitosamente' });

  } catch (error) {
    res.status(500).json({ error: 'Error al revocar suspensión', detalle: error.message });
  }
};

const actualizarPasswordAdmin = async (req, res) => {
  try {
    const { id } = req.params;
    const { nuevaPassword } = req.body;

    if (!nuevaPassword || nuevaPassword.length < 8) {
      return res.status(400).json({ error: 'La contraseña debe tener al menos 8 caracteres' });
    }

    const admin = await Admin.findByPk(id);
    if (!admin) return res.status(404).json({ error: 'Admin no encontrado' });
    if (admin.esSuperAdmin) return res.status(403).json({ error: 'No se puede modificar al SuperAdmin' });

    admin.password = nuevaPassword;
    await admin.save();

    res.json({ mensaje: 'Contraseña actualizada exitosamente' });

  } catch (error) {
    res.status(500).json({ error: 'Error al actualizar contraseña', detalle: error.message });
  }
};


module.exports = {
  loginAdmin,
  obtenerPerfilAdmin,
  crearAdmin,
  listarAdmins,
  eliminarAdmin,
  obtenerPublicacionesReportadas,
  obtenerUsuariosReportados,
  eliminarPublicacionAdmin,
  cerrarReporte,
  banearUsuario,
  banearUsuarioPermanente,
  eliminarReporte,
  revocarSuspension,
  actualizarPasswordAdmin
};