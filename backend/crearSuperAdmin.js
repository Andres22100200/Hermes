const bcrypt = require('bcryptjs');
const { sequelize } = require('./config/database');
const Admin = require('./models/Admin');

const crearSuperAdmin = async () => {
  try {
    await sequelize.authenticate();
    console.log('✅ Conectado a la base de datos');

    // Verificar si ya existe un superadmin
    const superAdminExistente = await Admin.findOne({ 
      where: { esSuperAdmin: true } 
    });

    if (superAdminExistente) {
      console.log('⚠️  Ya existe un SuperAdmin en la base de datos');
      console.log('Nombre:', superAdminExistente.nombre);
      console.log('Correo:', superAdminExistente.correo);
      process.exit(0);
    }

    // Crear el SuperAdmin
    const superAdmin = await Admin.create({
      nombre: 'SuperAdmin Hermes',
      correo: 'a22100200@ceti.mx',
      password: '49Andres4335',
      tipoAdmin: 'privilegiado',
      esSuperAdmin: true,
      activo: true,
      creadoPorAdminId: null
    });

    console.log('🎉 SuperAdmin creado exitosamente!');
    console.log('-----------------------------------');
    console.log('📧 Correo:', superAdmin.correo);
    console.log('🔑 Contraseña: 49Andres4335');
    console.log('-----------------------------------');
    console.log('⚠️  IMPORTANTE: Cambia esta contraseña después del primer login');

    process.exit(0);

  } catch (error) {
    console.error('❌ Error al crear SuperAdmin:', error);
    process.exit(1);
  }
};

crearSuperAdmin();