const { DataTypes } = require('sequelize');
const { sequelize } = require('../config/database');

const Publicacion = sequelize.define('Publicacion', {
  id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    autoIncrement: true
  },
  
  // Relación con el usuario vendedor
  usuarioId: {
    type: DataTypes.INTEGER,
    allowNull: false,
    references: {
      model: 'usuarios',
      key: 'id'
    }
  },
  
  // Información del libro
  titulo: {
    type: DataTypes.STRING(255),
    allowNull: false
  },
  
  autor: {
    type: DataTypes.STRING(255),
    allowNull: false
  },
  
  editorial: {
    type: DataTypes.STRING(255),
    allowNull: true
  },
  
  yearPublicacion: {
    type: DataTypes.INTEGER,
    allowNull: true
  },
  
  isbn: {
    type: DataTypes.STRING(20),
    allowNull: true
  },
  
  // Clasificación
  generos: {
    type: DataTypes.JSON,
    allowNull: false,
    comment: 'Array de géneros (máximo 3)'
  },
  
  // Estado del libro
  estadoLibro: {
    type: DataTypes.ENUM('Nuevo', 'Como nuevo', 'Muy bueno', 'Bueno', 'Aceptable'),
    allowNull: false
  },
  
  // Descripción y precio
  descripcion: {
    type: DataTypes.TEXT,
    allowNull: true
  },
  
  precio: {
    type: DataTypes.DECIMAL(10, 2),
    allowNull: false
  },
  
  // Fotos del libro (hasta 5)
  fotos: {
    type: DataTypes.JSON,
    allowNull: false,
    defaultValue: [],
    comment: 'Array de nombres de archivo (máximo 5)'
  },
  
  // Punto de encuentro
  puntoEncuentro: {
    type: DataTypes.STRING(255),
    allowNull: false
  },
  
  // Estado de la publicación
  estado: {
    type: DataTypes.ENUM('Disponible', 'Reservado', 'Vendido', 'Eliminado'),
    allowNull: false,
    defaultValue: 'Disponible'
  },
  
  // Moderación
  reportado: {
    type: DataTypes.BOOLEAN,
    defaultValue: false
  },
  
  motivoReporte: {
    type: DataTypes.TEXT,
    allowNull: true
  }
  
}, {
  tableName: 'publicaciones',
  timestamps: true
});

module.exports = Publicacion;