const express = require('express');
const cors = require('cors');
require('dotenv').config();

const { sequelize, testConnection } = require('./config/database');
const User = require('./models/User');
const Admin = require('./models/Admin');

const app = express();

// Middlewares
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

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


// Función para iniciar el servidor
const iniciarServidor = async () => {
  try {
    // Probar conexión a la base de datos
    await testConnection();
    
    // Sincronizar modelos con la base de datos
    // alter: true actualiza las tablas si ya existen
    // force: true elimina y recrea las tablas (¡CUIDADO en producción!)
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