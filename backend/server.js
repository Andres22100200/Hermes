const express = require('express');
const cors = require('cors');
require('dotenv').config();

const { sequelize, testConnection } = require('./config/database');
const User = require('./models/User');
const Admin = require('./models/Admin');
const Publicacion = require('./models/Publicacion');
const Conversacion = require('./models/Conversacion');
const Mensaje = require('./models/Mensaje');
const Valoracion = require('./models/Valoracion');
const Favorito = require('./models/Favorito');
const Reporte = require('./models/Reporte');

// ============= RELACIONES =============

// Relaciones Usuario-Publicacion
User.hasMany(Publicacion, { foreignKey: 'usuarioId', as: 'publicaciones' });
Publicacion.belongsTo(User, { foreignKey: 'usuarioId', as: 'vendedor' });

// Relaciones Conversacion
Conversacion.belongsTo(Publicacion, { foreignKey: 'publicacionId', as: 'publicacion' });
Conversacion.belongsTo(User, { foreignKey: 'compradorId', as: 'comprador' });
Conversacion.belongsTo(User, { foreignKey: 'vendedorId', as: 'vendedor' });

// Relaciones Mensaje
Mensaje.belongsTo(Conversacion, { foreignKey: 'conversacionId', as: 'conversacion' });
Mensaje.belongsTo(User, { foreignKey: 'remitenteId', as: 'remitente' });

// Relaciones Favorito
Favorito.belongsTo(Publicacion, { foreignKey: 'publicacionId', as: 'publicacion' });
Favorito.belongsTo(User, { foreignKey: 'usuarioId', as: 'usuario' });
User.hasMany(Favorito, { foreignKey: 'usuarioId', as: 'favoritos' });
Publicacion.hasMany(Favorito, { foreignKey: 'publicacionId', as: 'favoritos' });

// Relaciones Reporte
Reporte.belongsTo(User, { foreignKey: 'reportanteId', as: 'reportante' });
Reporte.belongsTo(User, { foreignKey: 'reportadoId', as: 'reportado' });
Reporte.belongsTo(Publicacion, { foreignKey: 'publicacionId', as: 'publicacion' });
User.hasMany(Reporte, { foreignKey: 'reportanteId', as: 'reportesEnviados' });
User.hasMany(Reporte, { foreignKey: 'reportadoId', as: 'reportesRecibidos' });
Publicacion.hasMany(Reporte, { foreignKey: 'publicacionId', as: 'reportes' });

const app = express();

// Middlewares
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Servir archivos estáticos (fotos de perfil)
app.use('/uploads', express.static('uploads'));

// Ruta de prueba
app.get('/', (req, res) => {
  res.json({ 
    mensaje: 'API de Hermes funcionando correctamente ✅',
    version: '1.0.0'
  });
});

// RUTAS DE LA API
const authRoutes = require('./routes/authRoutes');
app.use('/api/auth', authRoutes);

const adminRoutes = require('./routes/adminRoutes');
app.use('/api/admin', adminRoutes);

const profileRoutes = require('./routes/profileRoutes');
app.use('/api/profile', profileRoutes);

const publicacionRoutes = require('./routes/publicacionRoutes');
app.use('/api/publicaciones', publicacionRoutes);

const conversacionRoutes = require('./routes/conversacionRoutes');
app.use('/api/conversaciones', conversacionRoutes);

const valoracionRoutes = require('./routes/valoracionRoutes');
app.use('/api/valoraciones', valoracionRoutes);

const favoritoRoutes = require('./routes/favoritoRoutes');
app.use('/api/favoritos', favoritoRoutes);

const reporteRoutes = require('./routes/reporteRoutes');
app.use('/api/reportes', reporteRoutes);


const path = require('path'); //para el portal de admin
// Servir panel de administración
app.use('/admin', express.static(path.join(__dirname, '../admin-panel/dist')));

// Para que React Router funcione correctamente
app.get('/admin/*', (req, res) => {
  res.sendFile(path.join(__dirname, '../admin-panel/dist', 'index.html'));
});


const cron = require('node-cron');
const nodemailer = require('nodemailer');

// Configurar transporte de correo
const transporter = nodemailer.createTransport({
  service: 'gmail',
  auth: {
    user: process.env.EMAIL_USER,
    pass: process.env.EMAIL_PASS
  }
});

// Función para enviar resumen diario
const enviarResumenDiario = async () => {
  try {
    const { Op } = require('sequelize');
    const hace7Dias = new Date();
    hace7Dias.setDate(hace7Dias.getDate() - 7);

    // Obtener reportes pendientes
    const reportesPublicaciones = await Reporte.count({
      where: {
        tipo: 'publicacion',
        procesado: false,
        createdAt: { [Op.gte]: hace7Dias }
      }
    });

    const reportesUsuarios = await Reporte.count({
      where: {
        tipo: 'usuario',
        procesado: false,
        createdAt: { [Op.gte]: hace7Dias }
      }
    });

    if (reportesPublicaciones === 0 && reportesUsuarios === 0) {
      console.log('📧 No hay reportes pendientes, no se envía resumen.');
      return;
    }

    // Obtener todos los admins activos
    const admins = await Admin.findAll({
      where: { activo: true },
      attributes: ['correo', 'nombre']
    });

    const correosAdmins = admins.map(a => a.correo);

    const html = `
      <h2>📊 Resumen Diario - Hermes Marketplace</h2>
      <p>Fecha: ${new Date().toLocaleDateString('es-MX')}</p>
      <hr/>
      <h3>Reportes Pendientes</h3>
      <p>📚 Publicaciones reportadas: <strong>${reportesPublicaciones}</strong></p>
      <p>👤 Usuarios reportados: <strong>${reportesUsuarios}</strong></p>
      <hr/>
      <p>Accede al panel de administración para revisar y tomar acciones.</p>
    `;

    await transporter.sendMail({
      from: process.env.EMAIL_USER,
      to: correosAdmins,
      subject: `[Hermes] Resumen Diario - ${new Date().toLocaleDateString('es-MX')}`,
      html
    });

    console.log('📧 Resumen diario enviado a:', correosAdmins);

  } catch (error) {
    console.error('Error al enviar resumen diario:', error);
  }
};

// Cron job: cada día a medianoche
cron.schedule('0 0 * * *', () => {
  console.log('⏰ Ejecutando resumen diario...');
  enviarResumenDiario();
}, {
  timezone: 'America/Mexico_City'
});

// Función para iniciar el servidor
const iniciarServidor = async () => {
  try {
    // Probar conexión a la base de datos
    await testConnection();
    
    // Sincronizar modelos con la base de datos
    await sequelize.sync({ alter: false });
    console.log('✅ Modelos sincronizados con la base de datos');
    
    // Iniciar servidor
    const PORT = process.env.PORT || 3000;
    app.listen(PORT, () => {
      console.log(`🚀 Servidor corriendo en http://localhost:${PORT}`);
    });
    
  } catch (error) {
    console.error('❌ Error al iniciar el servidor:', error);
    process.exit(1);
  }
};

iniciarServidor();