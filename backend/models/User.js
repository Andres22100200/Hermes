const { DataTypes } = require('sequelize');
const { sequelize } = require('../config/database');
const bcrypt = require('bcryptjs');

const User = sequelize.define('User', {
  id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    autoIncrement: true
  },
  
  // Información básica obligatoria
  nombre: {
    type: DataTypes.STRING(100),
    allowNull: false,
    validate: {
      notEmpty: true
    }
  },
  
  apellido: {
    type: DataTypes.STRING(100),
    allowNull: false,
    validate: {
      notEmpty: true
    }
  },
  
  fechaNacimiento: {
    type: DataTypes.DATEONLY,
    allowNull: false,
    validate: {
      isDate: true,
      // Validación para mayores de 18 años (la haremos en el controlador)
    }
  },
  
  sexo: {
    type: DataTypes.ENUM('Masculino', 'Femenino', 'Otro', 'Prefiero no decir'),
    allowNull: false
  },
  
  numeroTelefonico: {
    type: DataTypes.STRING(15),
    allowNull: false,
    unique: true,
    validate: {
      isNumeric: true,
      len: [10, 15]
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
      len: [8, 255] // Mínimo 8 caracteres
    }
  },
  
  // Verificación de humanidad
  verificadoOTP: {
    type: DataTypes.BOOLEAN,
    defaultValue: false
  },
  
  codigoOTP: {
    type: DataTypes.STRING(6),
    allowNull: true
  },
  
  otpExpiracion: {
    type: DataTypes.DATE,
    allowNull: true
  },

  //RECUPERACION 
  tokenRecuperacion: {
    type: DataTypes.STRING(100),
    allowNull: true
  },
  
  tokenRecuperacionExpiracion: {
    type: DataTypes.DATE,
    allowNull: true
  },
  
  // Perfil y personalización (Módulo 2)
  fotoPerfil: {
    type: DataTypes.STRING(500),
    allowNull: true,
    defaultValue: null
  },
  
  biografia: {
    type: DataTypes.TEXT,
    allowNull: true,
    validate: {
      len: [0, 1000] // Máximo 200 palabras ≈ 1000 caracteres
    }
  },
  
  // Etiquetas de géneros literarios (guardadas como JSON)
  generosPreferidos: {
    type: DataTypes.JSON,
    allowNull: false,
    defaultValue: []
  },
  
  // Sistema de reputación (Módulo 6)
  promedioEstrellas_vendedor: {
    type: DataTypes.DECIMAL(2, 1), // Ejemplo: 4.5
    defaultValue: 0.0,
    validate: {
      min: 0.0,
      max: 5.0
    }
  },
  
  totalValoraciones_vendedor: {
    type: DataTypes.INTEGER,
    defaultValue: 0
  },
  
  promedioEstrellas_comprador: {
    type: DataTypes.DECIMAL(2, 1),
    defaultValue: 0.0,
    validate: {
      min: 0.0,
      max: 5.0
    }
  },
  
  totalValoraciones_comprador: {
    type: DataTypes.INTEGER,
    defaultValue: 0
  },
  
  // Estado de la cuenta
  activo: {
    type: DataTypes.BOOLEAN,
    defaultValue: true
  },
  
  suspendidoHasta: {
    type: DataTypes.DATE,
    allowNull: true,
    defaultValue: null
  },
  
  motivoSuspension: {
    type: DataTypes.TEXT,
    allowNull: true
  }
  
}, {
  timestamps: true, // Crea automáticamente createdAt y updatedAt
  tableName: 'usuarios'
});

// Hook para encriptar la contraseña antes de guardar
User.beforeCreate(async (user) => {
  if (user.password) {
    const salt = await bcrypt.genSalt(10);
    user.password = await bcrypt.hash(user.password, salt);
  }
});

// Hook para encriptar la contraseña antes de actualizar
User.beforeUpdate(async (user) => {
  if (user.changed('password')) {
    const salt = await bcrypt.genSalt(10);
    user.password = await bcrypt.hash(user.password, salt);
  }
});

// Método para comparar contraseñas
User.prototype.compararPassword = async function(passwordIngresado) {
  return await bcrypt.compare(passwordIngresado, this.password);
};

module.exports = User;