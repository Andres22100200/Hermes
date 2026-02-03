const nodemailer = require('nodemailer');

// Configuración del servicio de email
// Usaremos Gmail para enviar los OTPs (gratis)
const transporter = nodemailer.createTransport({
  service: 'gmail',
  auth: {
    user: process.env.EMAIL_USER, // Tu email de Gmail
    pass: process.env.EMAIL_PASSWORD // Contraseña de aplicación de Gmail
  }
});

/**
 * Genera un código OTP aleatorio de 6 dígitos
 * @returns {string} Código OTP (ejemplo: "123456")
 */
const generarCodigoOTP = () => {
  return Math.floor(100000 + Math.random() * 900000).toString();
};

/**
 * Envía un código OTP al correo del usuario
 * @param {string} correo - Email del destinatario
 * @param {string} codigoOTP - Código de 6 dígitos
 * @param {string} nombre - Nombre del usuario
 * @returns {Promise<boolean>} true si se envió correctamente
 */
const enviarOTP = async (correo, codigoOTP, nombre) => {
  try {
    const mailOptions = {
      from: process.env.EMAIL_USER,
      to: correo,
      subject: 'Código de verificación - Hermes',
      html: `
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
          <h2 style="color: #4A90E2;">¡Bienvenido a Hermes! 📚</h2>
          <p>Hola <strong>${nombre}</strong>,</p>
          <p>Tu código de verificación es:</p>
          <div style="background-color: #f4f4f4; padding: 20px; text-align: center; font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #333;">
            ${codigoOTP}
          </div>
          <p style="color: #666; font-size: 14px; margin-top: 20px;">
            Este código expirará en <strong>10 minutos</strong>.
          </p>
          <p style="color: #666; font-size: 14px;">
            Si no solicitaste este código, ignora este mensaje.
          </p>
          <hr style="border: none; border-top: 1px solid #ddd; margin: 20px 0;">
          <p style="color: #999; font-size: 12px; text-align: center;">
            Hermes - Marketplace de libros de segunda mano<br>
            Guadalajara, México
          </p>
        </div>
      `
    };

    await transporter.sendMail(mailOptions);
    console.log(`✅ OTP enviado a ${correo}`);
    return true;
    
  } catch (error) {
    console.error('❌ Error al enviar OTP:', error);
    return false;
  }
};

/**
 * Verifica si un código OTP es válido y no ha expirado
 * @param {Object} user - Objeto del usuario con codigoOTP y otpExpiracion
 * @param {string} codigoIngresado - Código que ingresó el usuario
 * @returns {boolean} true si el código es válido
 */
const verificarOTP = (user, codigoIngresado) => {
  // Verificar que el usuario tenga un OTP
  if (!user.codigoOTP || !user.otpExpiracion) {
    return false;
  }

  // Verificar que el código coincida
  if (user.codigoOTP !== codigoIngresado) {
    return false;
  }

  // Verificar que no haya expirado (10 minutos)
  const ahora = new Date();
  if (ahora > user.otpExpiracion) {
    return false;
  }

  return true;
};

module.exports = {
  generarCodigoOTP,
  enviarOTP,
  verificarOTP
};