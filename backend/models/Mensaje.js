const { DataTypes } = require('sequelize');
const { sequelize } = require('../config/database');

const Mensaje = sequelize.define('Mensaje', {
  id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    autoIncrement: true
  },
  conversacionId: {
    type: DataTypes.INTEGER,
    allowNull: false,
    references: {
      model: 'conversaciones',
      key: 'id'
    },
    onDelete: 'CASCADE'
  },
  remitenteId: {
    type: DataTypes.INTEGER,
    allowNull: false,
    references: {
      model: 'usuarios',
      key: 'id'
    },
    onDelete: 'CASCADE'
  },
  tipo: {
    type: DataTypes.ENUM('texto', 'imagen', 'sistema'),
    defaultValue: 'texto'
  },
  contenido: {
    type: DataTypes.TEXT,
    allowNull: false
  },
  // Para imágenes: nombre del archivo
  archivo: {
    type: DataTypes.STRING,
    allowNull: true
  },
  leido: {
    type: DataTypes.BOOLEAN,
    defaultValue: false
  }
}, {
  tableName: 'mensajes',
  timestamps: true
});

module.exports = Mensaje;