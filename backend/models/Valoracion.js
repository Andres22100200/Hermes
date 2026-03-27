const { DataTypes } = require('sequelize');
const { sequelize } = require('../config/database');

const Valoracion = sequelize.define('Valoracion', {
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
    }
  },
  emisorId: {
    type: DataTypes.INTEGER,
    allowNull: false,
    references: {
      model: 'usuarios',
      key: 'id'
    }
  },
  receptorId: {
    type: DataTypes.INTEGER,
    allowNull: false,
    references: {
      model: 'usuarios',
      key: 'id'
    }
  },
  // El rol del EMISOR en esta transacción
  rolEmisor: {
    type: DataTypes.ENUM('vendedor', 'comprador'),
    allowNull: false
  },
  estrellas: {
    type: DataTypes.INTEGER,
    allowNull: false,
    validate: {
      min: 1,
      max: 5
    }
  },
  etiquetas: {
    type: DataTypes.JSON,
    allowNull: false,
    defaultValue: []
  }
}, {
  tableName: 'valoraciones',
  timestamps: true
});

module.exports = Valoracion;