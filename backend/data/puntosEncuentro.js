const puntosEncuentro = [
  // ========== TREN LIGERO - LÍNEA 1 (Norte-Sur) ==========
  {
    id: 1,
    nombre: "Estación Periférico Norte",
    tipo: "Tren Ligero L1",
    zona: "Norte",
    coordenadas: { lat: 20.7331, lng: -103.3875 }
  },
  {
    id: 2,
    nombre: "Estación Dermatológico",
    tipo: "Tren Ligero L1",
    zona: "Norte",
    coordenadas: { lat: 20.7121, lng: -103.3443 }
  },
  {
    id: 3,
    nombre: "Estación Ávila Camacho",
    tipo: "Tren Ligero L1",
    zona: "Norte",
    coordenadas: { lat: 20.7025, lng: -103.3397 }
  },
  {
    id: 4,
    nombre: "Estación División del Norte",
    tipo: "Tren Ligero L1",
    zona: "Centro",
    coordenadas: { lat: 20.6925, lng: -103.3434 }
  },
  {
    id: 5,
    nombre: "Estación Atemajac",
    tipo: "Tren Ligero L1",
    zona: "Centro",
    coordenadas: { lat: 20.6891, lng: -103.3463 }
  },
  {
    id: 6,
    nombre: "Estación Juárez",
    tipo: "Tren Ligero L1",
    zona: "Centro",
    coordenadas: { lat: 20.6770, lng: -103.3467 }
  },
  {
    id: 7,
    nombre: "Estación Refugio",
    tipo: "Tren Ligero L1",
    zona: "Centro",
    coordenadas: { lat: 20.6689, lng: -103.3512 }
  },
  {
    id: 8,
    nombre: "Estación Mezquitán",
    tipo: "Tren Ligero L1",
    zona: "Sur",
    coordenadas: { lat: 20.6625, lng: -103.3556 }
  },
  {
    id: 9,
    nombre: "Estación Unidad Deportiva",
    tipo: "Tren Ligero L1",
    zona: "Sur",
    coordenadas: { lat: 20.6556, lng: -103.3589 }
  },
  {
    id: 10,
    nombre: "Estación Patria",
    tipo: "Tren Ligero L1",
    zona: "Sur",
    coordenadas: { lat: 20.6489, lng: -103.3623 }
  },
  
  // ========== TREN LIGERO - LÍNEA 2 (Este-Oeste) ==========
  {
    id: 11,
    nombre: "Estación Tetlán",
    tipo: "Tren Ligero L2",
    zona: "Este",
    coordenadas: { lat: 20.6589, lng: -103.2892 }
  },
  {
    id: 12,
    nombre: "Estación CUCEI",
    tipo: "Tren Ligero L2",
    zona: "Este",
    coordenadas: { lat: 20.6558, lng: -103.3251 }
  },
  {
    id: 13,
    nombre: "Estación Circunvalación",
    tipo: "Tren Ligero L2",
    zona: "Centro",
    coordenadas: { lat: 20.6689, lng: -103.3389 }
  },
  {
    id: 14,
    nombre: "Estación Plaza Universidad",
    tipo: "Tren Ligero L2",
    zona: "Centro",
    coordenadas: { lat: 20.6669, lng: -103.3611 }
  },
  {
    id: 15,
    nombre: "Estación Belisario Domínguez",
    tipo: "Tren Ligero L2",
    zona: "Oeste",
    coordenadas: { lat: 20.6725, lng: -103.3756 }
  },
  
  // ========== TREN LIGERO - LÍNEA 3 ==========
  {
    id: 16,
    nombre: "Estación Arcos de Guadalajara",
    tipo: "Tren Ligero L3",
    zona: "Oeste",
    coordenadas: { lat: 20.6956, lng: -103.4156 }
  },
  {
    id: 17,
    nombre: "Estación Glorieta Chapalita",
    tipo: "Tren Ligero L3",
    zona: "Oeste",
    coordenadas: { lat: 20.6736, lng: -103.3918 }
  },
  {
    id: 18,
    nombre: "Estación Terranova",
    tipo: "Tren Ligero L3",
    zona: "Oeste",
    coordenadas: { lat: 20.6834, lng: -103.3812 }
  },
  
  // ========== MACROBÚS ==========
  {
    id: 19,
    nombre: "Estación CUCEI (Macrobús)",
    tipo: "Macrobús",
    zona: "Este",
    coordenadas: { lat: 20.6558, lng: -103.3251 }
  },
  {
    id: 20,
    nombre: "Estación Independencia Sur",
    tipo: "Macrobús",
    zona: "Centro",
    coordenadas: { lat: 20.6689, lng: -103.3589 }
  },
  {
    id: 21,
    nombre: "Estación Agua Azul",
    tipo: "Macrobús",
    zona: "Sur",
    coordenadas: { lat: 20.6645, lng: -103.3467 }
  },
  
  // ========== PARQUES ==========
  {
    id: 22,
    nombre: "Parque Agua Azul",
    tipo: "Parque",
    zona: "Sur",
    coordenadas: { lat: 20.6689, lng: -103.3456 }
  },
  {
    id: 23,
    nombre: "Parque Revolución",
    tipo: "Parque",
    zona: "Centro",
    coordenadas: { lat: 20.6756, lng: -103.3467 }
  },
  {
    id: 24,
    nombre: "Parque Morelos",
    tipo: "Parque",
    zona: "Centro",
    coordenadas: { lat: 20.6812, lng: -103.3312 }
  },
  {
    id: 25,
    nombre: "Parque González Gallo",
    tipo: "Parque",
    zona: "Sur",
    coordenadas: { lat: 20.6523, lng: -103.3523 }
  },
  
  // ========== CENTRO HISTÓRICO ==========
  {
    id: 26,
    nombre: "Plaza de Armas",
    tipo: "Centro Histórico",
    zona: "Centro",
    coordenadas: { lat: 20.6767, lng: -103.3475 }
  },
  {
    id: 27,
    nombre: "Plaza Tapatía",
    tipo: "Centro Histórico",
    zona: "Centro",
    coordenadas: { lat: 20.6756, lng: -103.3423 }
  },
  {
    id: 28,
    nombre: "Plaza de la Liberación",
    tipo: "Centro Histórico",
    zona: "Centro",
    coordenadas: { lat: 20.6756, lng: -103.3456 }
  },
  {
    id: 29,
    nombre: "Mercado San Juan de Dios",
    tipo: "Centro Histórico",
    zona: "Centro",
    coordenadas: { lat: 20.6745, lng: -103.3398 }
  },
  {
    id: 30,
    nombre: "Plaza Guadalajara",
    tipo: "Centro Histórico",
    zona: "Centro",
    coordenadas: { lat: 20.6778, lng: -103.3489 }
  }
];

module.exports = puntosEncuentro;