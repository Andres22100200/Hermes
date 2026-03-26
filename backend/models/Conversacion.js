const { DataTypes } = require('sequelize');
const { sequelize } = require('../config/database');

const Conversacion = sequelize.define('Conversacion', {
  id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    autoIncrement: true
  },
  publicacionId: {
    type: DataTypes.INTEGER,
    allowNull: false,
    references: {
      model: 'publicaciones',
      key: 'id'
    },
    onDelete: 'CASCADE'
  },
  compradorId: {
    type: DataTypes.INTEGER,
    allowNull: false,
    references: {
      model: 'usuarios',
      key: 'id'
    },
    onDelete: 'CASCADE'
  },
  vendedorId: {
    type: DataTypes.INTEGER,
    allowNull: false,
    references: {
      model: 'usuarios',
      key: 'id'
    },
    onDelete: 'CASCADE'
  },
  // Estado de la transacción
  completada: {
    type: DataTypes.BOOLEAN,
    defaultValue: false
  },
  // Eliminación unilateral
  eliminadaPorComprador: {
    type: DataTypes.BOOLEAN,
    defaultValue: false
  },
  eliminadaPorVendedor: {
    type: DataTypes.BOOLEAN,
    defaultValue: false
  },
  // Último mensaje (para preview en lista)
  ultimoMensaje: {
    type: DataTypes.TEXT,
    allowNull: true
  },
  ultimoMensajeFecha: {
    type: DataTypes.DATE,
    allowNull: true
  }
}, {
  tableName: 'conversaciones',
  timestamps: true
});

module.exports = Conversacion;