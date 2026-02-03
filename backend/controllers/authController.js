const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const User = require('../models/User');
const { generarCodigoOTP, enviarOTP, verificarOTP } = require('../services/otpService');

/**
 * REGISTRO DE USUARIO
 * POST /api/auth/register
 */
const register = async (req, res) => {
  try {
    // 1. Extraer datos del body
    const {
      nombre,
      apellido,
      fechaNacimiento,
      sexo,
      numeroTelefonico,
      correo,
      password,
      generosPreferidos // Array de géneros seleccionados
    } = req.body;

    // 2. VALIDACIÓN: Campos obligatorios
    if (!nombre || !apellido || !fechaNacimiento || !sexo || !numeroTelefonico || !correo || !password) {
      return res.status(400).json({
        error: 'Todos los campos son obligatorios'
      });
    }

    // 3. VALIDACIÓN: Debe seleccionar al menos un género
    if (!generosPreferidos || generosPreferidos.length === 0) {
      return res.status(400).json({
        error: 'Debes seleccionar al menos un género literario de interés'
      });
    }

    // 4. VALIDACIÓN: Mayor de 18 años
    const fechaNac = new Date(fechaNacimiento);
    const hoy = new Date();
    let edad = hoy.getFullYear() - fechaNac.getFullYear();
    const mes = hoy.getMonth() - fechaNac.getMonth();
    
    if (mes < 0 || (mes === 0 && hoy.getDate() < fechaNac.getDate())) {
      edad--;
    }

    if (edad < 18) {
      return res.status(400).json({
        error: 'Debes ser mayor de 18 años para registrarte'
      });
    }

    // 5. VALIDACIÓN: Verificar si el correo ya existe
    const usuarioExistente = await User.findOne({ where: { correo } });
    if (usuarioExistente) {
      return res.status(400).json({
        error: 'Este correo ya está registrado'
      });
    }

    // 6. VALIDACIÓN: Verificar si el teléfono ya existe
    const telefonoExistente = await User.findOne({ where: { numeroTelefonico } });
    if (telefonoExistente) {
      return res.status(400).json({
        error: 'Este número telefónico ya está registrado'
      });
    }

    // 7. VALIDACIÓN: Contraseña mínimo 8 caracteres
    if (password.length < 8) {
      return res.status(400).json({
        error: 'La contraseña debe tener al menos 8 caracteres'
      });
    }

    // 8. Generar código OTP
    const codigoOTP = generarCodigoOTP();
    const otpExpiracion = new Date();
    otpExpiracion.setMinutes(otpExpiracion.getMinutes() + 10); // Expira en 10 minutos

    // 9. Crear el usuario en la base de datos
    const nuevoUsuario = await User.create({
      nombre,
      apellido,
      fechaNacimiento,
      sexo,
      numeroTelefonico,
      correo,
      password, // Se encripta automáticamente por el hook del modelo
      generosPreferidos,
      codigoOTP,
      otpExpiracion,
      verificadoOTP: false
    });

    // 10. Enviar código OTP por email
    const emailEnviado = await enviarOTP(correo, codigoOTP, nombre);
    
    if (!emailEnviado) {
      // Si no se pudo enviar el email, eliminamos el usuario creado
      await nuevoUsuario.destroy();
      return res.status(500).json({
        error: 'No se pudo enviar el código de verificación. Intenta de nuevo.'
      });
    }

    // 11. Respuesta exitosa
    res.status(201).json({
      mensaje: 'Usuario registrado exitosamente. Revisa tu correo para el código de verificación.',
      usuario: {
        id: nuevoUsuario.id,
        nombre: nuevoUsuario.nombre,
        correo: nuevoUsuario.correo,
        verificadoOTP: nuevoUsuario.verificadoOTP
      }
    });

  } catch (error) {
    console.error('Error en registro:', error);
    res.status(500).json({
      error: 'Error al registrar usuario',
      detalle: error.message
    });
  }
};

/**
 * VERIFICAR CÓDIGO OTP
 * POST /api/auth/verify-otp
 */
const verifyOTP = async (req, res) => {
  try {
    const { correo, codigoOTP } = req.body;

    // 1. Validación de campos
    if (!correo || !codigoOTP) {
      return res.status(400).json({
        error: 'Correo y código OTP son requeridos'
      });
    }

    // 2. Buscar usuario
    const usuario = await User.findOne({ where: { correo } });
    
    if (!usuario) {
      return res.status(404).json({
        error: 'Usuario no encontrado'
      });
    }

    // 3. Verificar si ya está verificado
    if (usuario.verificadoOTP) {
      return res.status(400).json({
        error: 'Este usuario ya ha sido verificado'
      });
    }

    // 4. Verificar el código OTP
    const otpValido = verificarOTP(usuario, codigoOTP);
    
    if (!otpValido) {
      return res.status(400).json({
        error: 'Código OTP inválido o expirado'
      });
    }

    // 5. Marcar como verificado
    usuario.verificadoOTP = true;
    usuario.codigoOTP = null;
    usuario.otpExpiracion = null;
    await usuario.save();

    // 6. Generar token JWT
    const token = jwt.sign(
      { 
        id: usuario.id,
        correo: usuario.correo,
        tipo: 'usuario'
      },
      process.env.JWT_SECRET,
      { expiresIn: '30d' } // Token válido por 30 días
    );

    // 7. Respuesta exitosa
    res.json({
      mensaje: 'Usuario verificado exitosamente',
      token,
      usuario: {
        id: usuario.id,
        nombre: usuario.nombre,
        apellido: usuario.apellido,
        correo: usuario.correo,
        verificadoOTP: usuario.verificadoOTP
      }
    });

  } catch (error) {
    console.error('Error en verificación OTP:', error);
    res.status(500).json({
      error: 'Error al verificar código OTP',
      detalle: error.message
    });
  }
};

/**
 * REENVIAR CÓDIGO OTP
 * POST /api/auth/resend-otp
 */
const resendOTP = async (req, res) => {
  try {
    const { correo } = req.body;

    // 1. Validación
    if (!correo) {
      return res.status(400).json({
        error: 'El correo es requerido'
      });
    }

    // 2. Buscar usuario
    const usuario = await User.findOne({ where: { correo } });
    
    if (!usuario) {
      return res.status(404).json({
        error: 'Usuario no encontrado'
      });
    }

    // 3. Verificar si ya está verificado
    if (usuario.verificadoOTP) {
      return res.status(400).json({
        error: 'Este usuario ya ha sido verificado'
      });
    }

    // 4. Generar nuevo código OTP
    const codigoOTP = generarCodigoOTP();
    const otpExpiracion = new Date();
    otpExpiracion.setMinutes(otpExpiracion.getMinutes() + 10);

    // 5. Actualizar en la base de datos
    usuario.codigoOTP = codigoOTP;
    usuario.otpExpiracion = otpExpiracion;
    await usuario.save();

    // 6. Enviar nuevo código
    const emailEnviado = await enviarOTP(correo, codigoOTP, usuario.nombre);
    
    if (!emailEnviado) {
      return res.status(500).json({
        error: 'No se pudo enviar el código de verificación. Intenta de nuevo.'
      });
    }

    // 7. Respuesta exitosa
    res.json({
      mensaje: 'Código OTP reenviado exitosamente'
    });

  } catch (error) {
    console.error('Error al reenviar OTP:', error);
    res.status(500).json({
      error: 'Error al reenviar código OTP',
      detalle: error.message
    });
  }
};

/**
 * LOGIN DE USUARIO
 * POST /api/auth/login
 */
const login = async (req, res) => {
  try {
    const { correo, password } = req.body;

    // 1. Validación de campos
    if (!correo || !password) {
      return res.status(400).json({
        error: 'Correo y contraseña son requeridos'
      });
    }

    // 2. Buscar usuario
    const usuario = await User.findOne({ where: { correo } });
    
    if (!usuario) {
      return res.status(401).json({
        error: 'Correo o contraseña incorrectos'
      });
    }

    // 3. Verificar que esté verificado con OTP
    if (!usuario.verificadoOTP) {
      return res.status(403).json({
        error: 'Debes verificar tu cuenta con el código OTP enviado a tu correo'
      });
    }

    // 4. Verificar que la cuenta esté activa
    if (!usuario.activo) {
      return res.status(403).json({
        error: 'Tu cuenta está desactivada. Contacta al soporte.'
      });
    }

    // 5. Verificar si está suspendido
    if (usuario.suspendidoHasta && new Date() < usuario.suspendidoHasta) {
      return res.status(403).json({
        error: 'Tu cuenta está suspendida temporalmente',
        suspendidoHasta: usuario.suspendidoHasta,
        motivo: usuario.motivoSuspension
      });
    }

    // 6. Verificar contraseña
    const passwordValido = await usuario.compararPassword(password);
    
    if (!passwordValido) {
      return res.status(401).json({
        error: 'Correo o contraseña incorrectos'
      });
    }

    // 7. Generar token JWT
    const token = jwt.sign(
      { 
        id: usuario.id,
        correo: usuario.correo,
        tipo: 'usuario'
      },
      process.env.JWT_SECRET,
      { expiresIn: '30d' }
    );

    // 8. Respuesta exitosa
    res.json({
      mensaje: 'Login exitoso',
      token,
      usuario: {
        id: usuario.id,
        nombre: usuario.nombre,
        apellido: usuario.apellido,
        correo: usuario.correo,
        fotoPerfil: usuario.fotoPerfil,
        promedioEstrellas_vendedor: usuario.promedioEstrellas_vendedor,
        promedioEstrellas_comprador: usuario.promedioEstrellas_comprador
      }
    });

  } catch (error) {
    console.error('Error en login:', error);
    res.status(500).json({
      error: 'Error al iniciar sesión',
      detalle: error.message
    });
  }
};

module.exports = {
  register,
  login,
  verifyOTP,
  resendOTP
};