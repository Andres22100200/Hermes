const User = require('./User');
const Publicacion = require('./Publicacion');
const Conversacion = require('./Conversacion');
const Mensaje = require('./Mensaje');
const Valoracion = require('./Valoracion');

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

module.exports = {
  User,
  Publicacion,
  Conversacion,
  Mensaje,
  Valoracion
};