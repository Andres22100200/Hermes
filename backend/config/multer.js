const multer = require('multer');
const path = require('path');
const fs = require('fs');

// Asegurar que existan las carpetas
const uploadsDir = path.join(__dirname, '..', 'uploads');
const profilePicsDir = path.join(uploadsDir, 'profile-pictures');
const bookPicsDir = path.join(uploadsDir, 'book-pictures');

if (!fs.existsSync(uploadsDir)) {
  fs.mkdirSync(uploadsDir);
}

if (!fs.existsSync(profilePicsDir)) {
  fs.mkdirSync(profilePicsDir);
}

if (!fs.existsSync(bookPicsDir)) {
  fs.mkdirSync(bookPicsDir);
}

// ========== CONFIGURACIÓN PARA FOTOS DE PERFIL ==========

const profileStorage = multer.diskStorage({
  destination: function (req, file, cb) {
    cb(null, profilePicsDir);
  },
  filename: function (req, file, cb) {
    const uniqueName = `user-${req.usuario.id}-${Date.now()}${path.extname(file.originalname)}`;
    cb(null, uniqueName);
  }
});

const profileFileFilter = (req, file, cb) => {
  const allowedTypes = /jpeg|jpg|png|gif/;
  const extname = allowedTypes.test(path.extname(file.originalname).toLowerCase());
  const mimetype = allowedTypes.test(file.mimetype);
  
  if (mimetype && extname) {
    return cb(null, true);
  } else {
    cb(new Error('Solo se permiten archivos de imagen (jpeg, jpg, png, gif)'));
  }
};

const uploadProfilePicture = multer({
  storage: profileStorage,
  limits: {
    fileSize: 5 * 1024 * 1024 // 5MB máximo
  },
  fileFilter: profileFileFilter
});

// ========== CONFIGURACIÓN PARA FOTOS DE LIBROS ==========

const bookStorage = multer.diskStorage({
  destination: function (req, file, cb) {
    cb(null, bookPicsDir);
  },
  filename: function (req, file, cb) {
    const uniqueName = `book-${req.usuario.id}-${Date.now()}-${Math.random().toString(36).substring(7)}${path.extname(file.originalname)}`;
    cb(null, uniqueName);
  }
});

const bookFileFilter = (req, file, cb) => {
  const allowedTypes = /jpeg|jpg|png|gif/;
  const extname = allowedTypes.test(path.extname(file.originalname).toLowerCase());
  const mimetype = allowedTypes.test(file.mimetype);
  
  if (mimetype && extname) {
    return cb(null, true);
  } else {
    cb(new Error('Solo se permiten archivos de imagen (jpeg, jpg, png, gif)'));
  }
};

const uploadBookPictures = multer({
  storage: bookStorage,
  limits: {
    fileSize: 5 * 1024 * 1024 // 5MB máximo por foto
  },
  fileFilter: bookFileFilter
});

module.exports = {
  uploadProfilePicture,
  uploadBookPictures
};