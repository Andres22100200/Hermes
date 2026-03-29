const User = require('./User');
const Publicacion = require('./Publicacion');
const Conversacion = require('./Conversacion');
const Mensaje = require('./Mensaje');
const Valoracion = require('./Valoracion');
const Favorito = require('./Favorito');

// Relaciones Conversacion
Conversacion.belongsTo(Publicacion, { foreignKey: 'publicacionId', as: 'publicacion' });
Conversacion.belongsTo(User, { foreignKey: 'compradorId', as: 'comprador' });
Conversacion.belongsTo(User, { foreignKey: 'vendedorId', as: 'vendedor' });

// Relaciones Mensaje
Mensaje.belongsTo(Conversacion, { foreignKey: 'conversacionId', as: 'conversacion' });
Mensaje.belongsTo(User, { foreignKey: 'remitenteId', as: 'remitente' });

// Relaciones Valoracion
Valoracion.belongsTo(Conversacion, { foreignKey: 'conversacionId', as: 'conversacion' });
Valoracion.belongsTo(User, { foreignKey: 'emisorId', as: 'emisor' });
Valoracion.belongsTo(User, { foreignKey: 'receptorId', as: 'receptor' });

// Relaciones Favorito
Favorito.belongsTo(User, { foreignKey: 'usuarioId', as: 'usuario' });
Favorito.belongsTo(Publicacion, { foreignKey: 'publicacionId', as: 'publicacion' });
User.hasMany(Favorito, { foreignKey: 'usuarioId', as: 'favoritos' });
Publicacion.hasMany(Favorito, { foreignKey: 'publicacionId', as: 'favoritos' });

module.exports = {
  User,
  Publicacion,
  Conversacion,
  Mensaje,
  Valoracion,
  Favorito
};