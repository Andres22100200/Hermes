const jwt = require('jsonwebtoken');
const User = require('../models/User');
const Admin = require('../models/Admin');

/**
 * Middleware para verificar token JWT de usuarios
 * Protege rutas que requieren autenticación
 */
const verificarToken = async (req, res, next) => {
  try {
    // 1. Obtener token del header Authorization
    const authHeader = req.headers.authorization;
    
    if (!authHeader) {
      return res.status(401).json({
        error: 'No se proporcionó token de autenticación'
      });
    }

    // 2. El token viene en formato: "Bearer eyJhbGciOiJIUzI1..."
    // Extraemos solo el token
    const token = authHeader.split(' ')[1];
    
    if (!token) {
      return res.status(401).json({
        error: 'Formato de token inválido'
      });
    }

    // 3. Verificar y decodificar el token
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    
    // 4. Buscar el usuario en la base de datos
    const usuario = await User.findByPk(decoded.id);
    
    if (!usuario) {
      return res.status(401).json({
        error: 'Usuario no encontrado'
      });
    }

    // 5. Verificar que el usuario esté activo
    if (!usuario.activo) {
      return res.status(403).json({
        error: 'Tu cuenta está desactivada'
      });
    }

    // 6. Verificar si está suspendido
    if (usuario.suspendidoHasta && new Date() < usuario.suspendidoHasta) {
      return res.status(403).json({
        error: 'Tu cuenta está suspendida temporalmente',
        suspendidoHasta: usuario.suspendidoHasta
      });
    }

    // 7. Agregar el usuario al objeto req para usarlo en las rutas
    req.usuario = usuario;
    
    // 8. Continuar con la siguiente función
    next();

  } catch (error) {
    if (error.name === 'JsonWebTokenError') {
      return res.status(401).json({
        error: 'Token inválido'
      });
    }
    
    if (error.name === 'TokenExpiredError') {
      return res.status(401).json({
        error: 'Token expirado. Por favor inicia sesión de nuevo'
      });
    }

    console.error('Error en verificarToken:', error);
    res.status(500).json({
      error: 'Error al verificar token',
      detalle: error.message
    });
  }
};

/**
 * Middleware para verificar token JWT de administradores
 */
const verificarTokenAdmin = async (req, res, next) => {
  try {
    const authHeader = req.headers.authorization;
    
    if (!authHeader) {
      return res.status(401).json({
        error: 'No se proporcionó token de autenticación'
      });
    }

    const token = authHeader.split(' ')[1];
    
    if (!token) {
      return res.status(401).json({
        error: 'Formato de token inválido'
      });
    }

    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    
    // Verificar que el token sea de tipo admin
    if (decoded.tipo !== 'admin') {
      return res.status(403).json({
        error: 'Acceso denegado. Se requiere cuenta de administrador'
      });
    }

    const admin = await Admin.findByPk(decoded.id);
    
    if (!admin) {
      return res.status(401).json({
        error: 'Administrador no encontrado'
      });
    }

    if (!admin.activo) {
      return res.status(403).json({
        error: 'Tu cuenta de administrador está desactivada'
      });
    }

    req.admin = admin;
    next();

  } catch (error) {
    if (error.name === 'JsonWebTokenError') {
      return res.status(401).json({
        error: 'Token inválido'
      });
    }
    
    if (error.name === 'TokenExpiredError') {
      return res.status(401).json({
        error: 'Token expirado. Por favor inicia sesión de nuevo'
      });
    }

    console.error('Error en verificarTokenAdmin:', error);
    res.status(500).json({
      error: 'Error al verificar token',
      detalle: error.message
    });
  }
};

/**
 * Middleware para verificar que el admin sea privilegiado (superadmin)
 */
const verificarAdminPrivilegiado = (req, res, next) => {
  if (!req.admin) {
    return res.status(401).json({
      error: 'No autenticado como administrador'
    });
  }

  if (req.admin.tipoAdmin !== 'privilegiado') {
    return res.status(403).json({
      error: 'Acceso denegado. Se requieren permisos de administrador privilegiado'
    });
  }

  next();
};


module.exports = {
  verificarToken,
  verificarTokenAdmin,
  verificarAdminPrivilegiado
};