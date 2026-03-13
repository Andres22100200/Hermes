const puntosEncuentro = [
  // ========== TREN LIGERO - LÍNEA 1 (Norte-Sur) ==========
  {
    id: 1,
    nombre: "Auditorio",
    tipo: "Tren Ligero L1",
    zona: "Norte",
    coordenadas: { lat: 20.7456, lng: -103.3923 }
  },
  {
    id: 2,
    nombre: "Periférico Norte",
    tipo: "Tren Ligero L1",
    zona: "Norte",
    coordenadas: { lat: 20.7331, lng: -103.3875 }
  },
  {
    id: 3,
    nombre: "Dermatológico",
    tipo: "Tren Ligero L1",
    zona: "Norte",
    coordenadas: { lat: 20.7121, lng: -103.3443 }
  },
  {
    id: 4,
    nombre: "Atemajac",
    tipo: "Tren Ligero L1",
    zona: "Norte",
    coordenadas: { lat: 20.6891, lng: -103.3463 }
  },
  {
    id: 5,
    nombre: "División del Norte",
    tipo: "Tren Ligero L1",
    zona: "Centro",
    coordenadas: { lat: 20.6925, lng: -103.3434 }
  },
  {
    id: 6,
    nombre: "Ávila Camacho",
    tipo: "Tren Ligero L1",
    zona: "Centro",
    coordenadas: { lat: 20.7025, lng: -103.3397 }
  },
  {
    id: 7,
    nombre: "Mezquitán",
    tipo: "Tren Ligero L1",
    zona: "Centro",
    coordenadas: { lat: 20.6625, lng: -103.3556 }
  },
  {
    id: 8,
    nombre: "Refugio",
    tipo: "Tren Ligero L1",
    zona: "Centro",
    coordenadas: { lat: 20.6689, lng: -103.3512 }
  },
  {
    id: 9,
    nombre: "Juárez",
    tipo: "Tren Ligero L1",
    zona: "Centro",
    coordenadas: { lat: 20.6770, lng: -103.3467 }
  },
  {
    id: 10,
    nombre: "Mexicaltzingo",
    tipo: "Tren Ligero L1",
    zona: "Centro",
    coordenadas: { lat: 20.6734, lng: -103.3523 }
  },
  {
    id: 11,
    nombre: "Washington",
    tipo: "Tren Ligero L1",
    zona: "Sur",
    coordenadas: { lat: 20.6678, lng: -103.3567 }
  },
  {
    id: 12,
    nombre: "Santa Filomena",
    tipo: "Tren Ligero L1",
    zona: "Sur",
    coordenadas: { lat: 20.6612, lng: -103.3589 }
  },
  {
    id: 13,
    nombre: "Unidad Deportiva",
    tipo: "Tren Ligero L1",
    zona: "Sur",
    coordenadas: { lat: 20.6556, lng: -103.3589 }
  },
  {
    id: 14,
    nombre: "Urdaneta",
    tipo: "Tren Ligero L1",
    zona: "Sur",
    coordenadas: { lat: 20.6512, lng: -103.3612 }
  },
  {
    id: 15,
    nombre: "18 de Marzo",
    tipo: "Tren Ligero L1",
    zona: "Sur",
    coordenadas: { lat: 20.6467, lng: -103.3645 }
  },
  {
    id: 16,
    nombre: "Isla Raza",
    tipo: "Tren Ligero L1",
    zona: "Sur",
    coordenadas: { lat: 20.6423, lng: -103.3678 }
  },
  {
    id: 17,
    nombre: "Patria",
    tipo: "Tren Ligero L1",
    zona: "Sur",
    coordenadas: { lat: 20.6489, lng: -103.3623 }
  },
  {
    id: 18,
    nombre: "España",
    tipo: "Tren Ligero L1",
    zona: "Sur",
    coordenadas: { lat: 20.6389, lng: -103.3712 }
  },
  {
    id: 19,
    nombre: "Santuario Mártires de Cristo Rey",
    tipo: "Tren Ligero L1",
    zona: "Sur",
    coordenadas: { lat: 20.6345, lng: -103.3745 }
  },
  {
    id: 20,
    nombre: "Periférico Sur",
    tipo: "Tren Ligero L1",
    zona: "Sur",
    coordenadas: { lat: 20.6289, lng: -103.3778 }
  },
  
  // ========== TREN LIGERO - LÍNEA 2 (Este-Oeste) ==========
  {
    id: 21,
    nombre: "Juárez",
    tipo: "Tren Ligero L2",
    zona: "Centro",
    coordenadas: { lat: 20.6770, lng: -103.3467 }
  },
  {
    id: 22,
    nombre: "Plaza Universidad",
    tipo: "Tren Ligero L2",
    zona: "Centro",
    coordenadas: { lat: 20.6669, lng: -103.3611 }
  },
  {
    id: 23,
    nombre: "San Andrés",
    tipo: "Tren Ligero L2",
    zona: "Este",
    coordenadas: { lat: 20.6645, lng: -103.3389 }
  },
  {
    id: 24,
    nombre: "Cristóbal de Oñate",
    tipo: "Tren Ligero L2",
    zona: "Este",
    coordenadas: { lat: 20.6623, lng: -103.3245 }
  },
  {
    id: 25,
    nombre: "Mexicaltzingo",
    tipo: "Tren Ligero L2",
    zona: "Centro",
    coordenadas: { lat: 20.6734, lng: -103.3523 }
  },
  {
    id: 26,
    nombre: "San Juan de Dios",
    tipo: "Tren Ligero L2",
    zona: "Centro",
    coordenadas: { lat: 20.6756, lng: -103.3412 }
  },
  
  // ========== TREN LIGERO - LÍNEA 3 ==========
  {
    id: 27,
    nombre: "Arcos de Zapopan",
    tipo: "Tren Ligero L3",
    zona: "Oeste",
    coordenadas: { lat: 20.6956, lng: -103.4156 }
  },
  {
    id: 28,
    nombre: "Periférico Belenes",
    tipo: "Tren Ligero L3",
    zona: "Oeste",
    coordenadas: { lat: 20.6889, lng: -103.4089 }
  },
  {
    id: 29,
    nombre: "Mercado del Mar",
    tipo: "Tren Ligero L3",
    zona: "Oeste",
    coordenadas: { lat: 20.6823, lng: -103.4023 }
  },
  {
    id: 30,
    nombre: "Zapopan Centro",
    tipo: "Tren Ligero L3",
    zona: "Oeste",
    coordenadas: { lat: 20.7234, lng: -103.3912 }
  },
  {
    id: 31,
    nombre: "Plaza Patria",
    tipo: "Tren Ligero L3",
    zona: "Oeste",
    coordenadas: { lat: 20.6834, lng: -103.3945 }
  },
  {
    id: 32,
    nombre: "Circunvalación Country",
    tipo: "Tren Ligero L3",
    zona: "Oeste",
    coordenadas: { lat: 20.6778, lng: -103.3889 }
  },
  {
    id: 33,
    nombre: "Ávila Camacho",
    tipo: "Tren Ligero L3",
    zona: "Centro",
    coordenadas: { lat: 20.7025, lng: -103.3397 }
  },
  {
    id: 34,
    nombre: "La Normal",
    tipo: "Tren Ligero L3",
    zona: "Centro",
    coordenadas: { lat: 20.6712, lng: -103.3823 }
  },
  {
    id: 35,
    nombre: "Santuario",
    tipo: "Tren Ligero L3",
    zona: "Centro",
    coordenadas: { lat: 20.6689, lng: -103.3756 }
  },
  {
    id: 36,
    nombre: "Guadalajara Centro",
    tipo: "Tren Ligero L3",
    zona: "Centro",
    coordenadas: { lat: 20.6767, lng: -103.3475 }
  },
  {
    id: 37,
    nombre: "Independencia",
    tipo: "Tren Ligero L3",
    zona: "Centro",
    coordenadas: { lat: 20.6734, lng: -103.3612 }
  },
  {
    id: 38,
    nombre: "Plaza de la Bandera",
    tipo: "Tren Ligero L3",
    zona: "Este",
    coordenadas: { lat: 20.6689, lng: -103.3489 }
  },
  {
    id: 39,
    nombre: "CUCEI",
    tipo: "Tren Ligero L3",
    zona: "Este",
    coordenadas: { lat: 20.6558, lng: -103.3251 }
  },
  {
    id: 40,
    nombre: "Revolución",
    tipo: "Tren Ligero L3",
    zona: "Este",
    coordenadas: { lat: 20.6612, lng: -103.3178 }
  },
  {
    id: 41,
    nombre: "Río Nilo",
    tipo: "Tren Ligero L3",
    zona: "Este",
    coordenadas: { lat: 20.6567, lng: -103.3089 }
  },
  {
    id: 42,
    nombre: "Tlaquepaque Centro",
    tipo: "Tren Ligero L3",
    zona: "Este",
    coordenadas: { lat: 20.6456, lng: -103.3123 }
  },
  {
    id: 43,
    nombre: "Lázaro Cárdenas",
    tipo: "Tren Ligero L3",
    zona: "Este",
    coordenadas: { lat: 20.6389, lng: -103.2945 }
  },
  {
    id: 44,
    nombre: "Central de Autobuses",
    tipo: "Tren Ligero L3",
    zona: "Sur",
    coordenadas: { lat: 20.6234, lng: -103.3567 }
  },
  
  // ========== PLAZAS COMERCIALES ==========
  {
    id: 45,
    nombre: "Gran Plaza",
    tipo: "Plaza Comercial",
    zona: "Oeste",
    coordenadas: { lat: 20.6789, lng: -103.3967 }
  },
  {
    id: 46,
    nombre: "Plaza Galerías",
    tipo: "Plaza Comercial",
    zona: "Oeste",
    coordenadas: { lat: 20.6734, lng: -103.3912 }
  }
];

module.exports = puntosEncuentro;