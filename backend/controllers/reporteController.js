const Reporte = require('../models/Reporte');
const User = require('../models/User');
const Publicacion = require('../models/Publicacion');
const { Op, literal } = require('sequelize');

const DIAS_VIGENCIA = 1;

const getVigenciaFecha = () => {
  const fecha = new Date();
  fecha.setDate(fecha.getDate() - DIAS_VIGENCIA);
  return fecha;
};

/**
 * CREAR REPORTE DE PUBLICACIÓN
 * POST /api/reportes/publicacion
 */
const reportarPublicacion = async (req, res) => {
  try {
    const { publicacionId, categoria, descripcion } = req.body;
    const reportanteId = req.usuario.id;

    if (!publicacionId || !categoria) {
      return res.status(400).json({ error: 'publicacionId y categoria son requeridos' });
    }

    const publicacion = await Publicacion.findByPk(publicacionId);
    if (!publicacion) {
      return res.status(404).json({ error: 'Publicación no encontrada' });
    }

    if (publicacion.usuarioId === reportanteId) {
      return res.status(400).json({ error: 'No puedes reportar tu propia publicación' });
    }

    const hoyInicio = new Date();
    hoyInicio.setHours(0, 0, 0, 0);

    // Límite global: 6 reportes por día
    const reportesHoy = await Reporte.count({
      where: {
        reportanteId,
        createdAt: { [Op.gte]: hoyInicio }
      }
    });

    if (reportesHoy >= 6) {
      return res.status(400).json({ error: 'Has alcanzado el límite de 6 reportes por día' });
    }

    // Límite por objetivo: máximo 3 reportes al mismo vendedor hoy
    const vendedorId = publicacion.usuarioId;
    const reportesAlMismoUsuarioHoy = await Reporte.count({
      where: {
        reportanteId,
        createdAt: { [Op.gte]: hoyInicio },
        [Op.or]: [
          { reportadoId: vendedorId },
          {
            publicacionId: {
              [Op.in]: literal(`(SELECT id FROM publicaciones WHERE usuarioId = ${vendedorId})`)
            }
          }
        ]
      }
    });

    if (reportesAlMismoUsuarioHoy >= 3) {
      return res.status(400).json({ error: 'Ya has reportado 3 veces a este usuario hoy' });
    }

    // Verificar que no haya reportado esta publicación antes (en los últimos 7 días)
    const reporteExistente = await Reporte.findOne({
      where: {
        reportanteId,
        publicacionId,
        tipo: 'publicacion',
        createdAt: { [Op.gte]: getVigenciaFecha() }
      }
    });

    if (reporteExistente) {
      return res.status(400).json({ error: 'Ya reportaste esta publicación recientemente' });
    }

    const reporte = await Reporte.create({
      reportanteId,
      tipo: 'publicacion',
      publicacionId,
      categoria,
      descripcion: descripcion || null
    });

    const totalReportes = await Reporte.count({
      where: {
        publicacionId,
        tipo: 'publicacion',
        createdAt: { [Op.gte]: getVigenciaFecha() }
      }
    });

    // Acciones automáticas
    if (totalReportes >= 5) {
      publicacion.estado = 'Eliminado';
      await publicacion.save();

      if (totalReportes >= 10) {
        const vendedor = await User.findByPk(vendedorId);
        if (vendedor) {
          const suspension = new Date();
          suspension.setHours(suspension.getHours() + 24);
          vendedor.suspendidoHasta = suspension;
          vendedor.motivoSuspension = 'Publicación con 10 o más reportes';
          await vendedor.save();
        }
      }
    }

    res.status(201).json({ mensaje: 'Reporte enviado', totalReportes });

  } catch (error) {
    console.error('Error al reportar publicación:', error);
    res.status(500).json({ error: 'Error al reportar publicación', detalle: error.message });
  }
};

/**
 * CREAR REPORTE DE USUARIO
 * POST /api/reportes/usuario
 */
const reportarUsuario = async (req, res) => {
  try {
    const { reportadoId, categoria, descripcion } = req.body;
    const reportanteId = req.usuario.id;

    if (!reportadoId || !categoria) {
      return res.status(400).json({ error: 'reportadoId y categoria son requeridos' });
    }

    if (parseInt(reportadoId) === reportanteId) {
      return res.status(400).json({ error: 'No puedes reportarte a ti mismo' });
    }

    const reportado = await User.findByPk(reportadoId);
    if (!reportado) {
      return res.status(404).json({ error: 'Usuario no encontrado' });
    }

    const hoyInicio = new Date();
    hoyInicio.setHours(0, 0, 0, 0);

    // Límite global: 6 reportes por día
    const reportesHoy = await Reporte.count({
      where: {
        reportanteId,
        createdAt: { [Op.gte]: hoyInicio }
      }
    });

    if (reportesHoy >= 6) {
      return res.status(400).json({ error: 'Has alcanzado el límite de 6 reportes por día' });
    }

    // Límite por objetivo: máximo 3 reportes al mismo usuario hoy
    const reportesAlMismoUsuarioHoy = await Reporte.count({
      where: {
        reportanteId,
        createdAt: { [Op.gte]: hoyInicio },
        [Op.or]: [
          { reportadoId },
          {
            publicacionId: {
              [Op.in]: literal(`(SELECT id FROM publicaciones WHERE usuarioId = ${reportadoId})`)
            }
          }
        ]
      }
    });

    if (reportesAlMismoUsuarioHoy >= 3) {
      return res.status(400).json({ error: 'Ya has reportado 3 veces a este usuario hoy' });
    }

    // Verificar que no haya reportado este usuario antes (en los últimos 7 días)
    const reporteExistente = await Reporte.findOne({
      where: {
        reportanteId,
        reportadoId,
        tipo: 'usuario',
        createdAt: { [Op.gte]: getVigenciaFecha() }
      }
    });

    if (reporteExistente) {
      return res.status(400).json({ error: 'Ya reportaste a este usuario recientemente' });
    }

    await Reporte.create({
      reportanteId,
      tipo: 'usuario',
      reportadoId,
      categoria,
      descripcion: descripcion || null
    });

    const totalReportes = await Reporte.count({
      where: {
        reportadoId,
        tipo: 'usuario',
        createdAt: { [Op.gte]: getVigenciaFecha() }
      }
    });

    // Acciones automáticas
    if (totalReportes >= 5) {
      if (totalReportes >= 10) {
        reportado.activo = false;
        reportado.motivoSuspension = 'Suspensión permanente por 10 o más reportes';
      } else {
        const suspension = new Date();
        suspension.setHours(suspension.getHours() + 24);
        reportado.suspendidoHasta = suspension;
        reportado.motivoSuspension = 'Suspensión temporal por 5 o más reportes';
      }
      await reportado.save();
    }

    res.status(201).json({ mensaje: 'Usuario reportado', totalReportes });

  } catch (error) {
    console.error('Error al reportar usuario:', error);
    res.status(500).json({ error: 'Error al reportar usuario', detalle: error.message });
  }
};

/**
 * OBTENER REPORTES DE UNA PUBLICACIÓN (para admins)
 * GET /api/reportes/publicacion/:id
 */
const obtenerReportesPublicacion = async (req, res) => {
  try {
    const { id } = req.params;

    const reportes = await Reporte.findAll({
      where: {
        publicacionId: id,
        tipo: 'publicacion',
        createdAt: { [Op.gte]: getVigenciaFecha() }
      },
      order: [['createdAt', 'DESC']]
    });

    res.json({ total: reportes.length, reportes });

  } catch (error) {
    res.status(500).json({ error: 'Error al obtener reportes', detalle: error.message });
  }
};

/**
 * OBTENER REPORTES DE UN USUARIO (para admins)
 * GET /api/reportes/usuario/:id
 */
const obtenerReportesUsuario = async (req, res) => {
  try {
    const { id } = req.params;

    const reportes = await Reporte.findAll({
      where: {
        reportadoId: id,
        tipo: 'usuario',
        createdAt: { [Op.gte]: getVigenciaFecha() }
      },
      order: [['createdAt', 'DESC']]
    });

    res.json({ total: reportes.length, reportes });

  } catch (error) {
    res.status(500).json({ error: 'Error al obtener reportes', detalle: error.message });
  }
};

module.exports = {
  reportarPublicacion,
  reportarUsuario,
  obtenerReportesPublicacion,
  obtenerReportesUsuario
};