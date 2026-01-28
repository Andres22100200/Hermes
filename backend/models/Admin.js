const { DataTypes } = require('sequelize');
const { sequelize } = require('../config/database');
const bcrypt = require('bcryptjs');

const Admin = sequelize.define('Admin', {
  id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    autoIncrement: true
  },
  
  nombre: {
    type: DataTypes.STRING(100),
    allowNull: false,
    validate: {
      notEmpty: true
    }
  },
  
  correo: {
    type: DataTypes.STRING(255),
    allowNull: false,
    unique: true,
    validate: {
      isEmail: true
    }
  },
  
  password: {
    type: DataTypes.STRING(255),
    allowNull: false,
    validate: {
      len: [8, 255]
    }
  },
  
  // Jerarquía de administradores (Módulo 1.D)
  tipoAdmin: {
    type: DataTypes.ENUM('privilegiado', 'comun'),
    allowNull: false,
    defaultValue: 'comun'
  },
  
  // Indica si es el superadmin inicial (el primero creado desde DB)
  esSuperAdmin: {
    type: DataTypes.BOOLEAN,
    defaultValue: false
  },
  
  // Estado de la cuenta
  activo: {
    type: DataTypes.BOOLEAN,
    defaultValue: true
  },
  
  // Auditoría: quién lo creó
  creadoPorAdminId: {
    type: DataTypes.INTEGER,
    allowNull: true, // El primer admin no fue creado por nadie
    references: {
      model: 'administradores',
      key: 'id'
    }
  }
  
}, {
  timestamps: true,
  tableName: 'administradores'
});

// Hook para encriptar la contraseña antes de guardar
Admin.beforeCreate(async (admin) => {
  if (admin.password) {
    const salt = await bcrypt.genSalt(10);
    admin.password = await bcrypt.hash(admin.password, salt);
  }
});

// Hook para encriptar la contraseña antes de actualizar
Admin.beforeUpdate(async (admin) => {
  if (admin.changed('password')) {
    const salt = await bcrypt.genSalt(10);
    admin.password = await bcrypt.hash(admin.password, salt);
  }
});

// Método para comparar contraseñas
Admin.prototype.compararPassword = async function(passwordIngresado) {
  return await bcrypt.compare(passwordIngresado, this.password);
};

module.exports = Admin;