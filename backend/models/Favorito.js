const { DataTypes } = require('sequelize');
const { sequelize } = require('../config/database');

const Favorito = sequelize.define('Favorito', {
  id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    autoIncrement: true
  },
  usuarioId: {
    type: DataTypes.INTEGER,
    allowNull: false,
    references: {
      model: 'usuarios',
      key: 'id'
    },
    onDelete: 'CASCADE'
  },
  publicacionId: {
    type: DataTypes.INTEGER,
    allowNull: false,
    references: {
      model: 'publicaciones',
      key: 'id'
    },
    onDelete: 'CASCADE'
  }
}, {
  tableName: 'favoritos',
  timestamps: true,
  indexes: [
    {
      unique: true,
      fields: ['usuarioId', 'publicacionId']
    }
  ]
});

module.exports = Favorito;