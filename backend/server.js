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

// Función para iniciar el servidor
const iniciarServidor = async () => {
  try {
    // Probar conexión a la base de datos
    await testConnection();
    
    // Sincronizar modelos con la base de datos
    await sequelize.sync({ alter: true });
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