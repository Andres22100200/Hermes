const jwt = require('jsonwebtoken');
const Admin = require('../models/Admin');

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

module.exports = {
  loginAdmin,
  obtenerPerfilAdmin,
  crearAdmin,
  listarAdmins,
  eliminarAdmin
};