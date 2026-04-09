const { DataTypes } = require('sequelize');
const { sequelize } = require('../config/database');

const Reporte = sequelize.define('Reporte', {
  id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    autoIncrement: true
  },
  reportanteId: {
    type: DataTypes.INTEGER,
    allowNull: false,
    references: { model: 'usuarios', key: 'id' },
    onDelete: 'CASCADE'
  },
  // Tipo de reporte: 'publicacion' o 'usuario'
  tipo: {
    type: DataTypes.ENUM('publicacion', 'usuario'),
    allowNull: false
  },
  // ID del objeto reportado
  publicacionId: {
    type: DataTypes.INTEGER,
    allowNull: true,
    references: { model: 'publicaciones', key: 'id' },
    onDelete: 'CASCADE'
  },
  reportadoId: {
    type: DataTypes.INTEGER,
    allowNull: true,
    references: { model: 'usuarios', key: 'id' },
    onDelete: 'CASCADE'
  },
  categoria: {
    type: DataTypes.STRING(100),
    allowNull: false
  },
  descripcion: {
    type: DataTypes.TEXT,
    allowNull: true
  },
  procesado: {
    type: DataTypes.BOOLEAN,
    defaultValue: false
  }
}, {
  tableName: 'reportes',
  timestamps: true
});

module.exports = Reporte;