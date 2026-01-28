\# Hermes - Marketplace de Libros de Segunda Mano 📚



Plataforma móvil para la compra y venta de libros de segunda mano en Guadalajara, México.



\## 🏗️ Estructura del Proyecto

```

Hermes/

├── backend/          # API REST con Node.js + Express + MariaDB

└── android/          # Aplicación móvil Android (Java)

```



\## 🛠️ Tecnologías



\### Backend

\- Node.js + Express

\- MariaDB (MySQL)

\- Sequelize ORM

\- JWT Authentication

\- Socket.IO (Chat en tiempo real)



\### Android

\- Android Studio (API 29)

\- Java

\- MVVM Architecture

\- Material Design



\## 📋 Módulos Principales



1\. Registro y Autenticación (con OTP)

2\. Perfil Universal y Reputación

3\. Publicación de Ofertas

4\. Gestión de Ventas

5\. Comunicación Directa (Chat)

6\. Valoración y Confianza

7\. Logística y Puntos de Entrega

8\. Favoritos

9\. Sistema de Reportes

10\. Edición de Perfil

11\. Panel de Administrador

12\. Analítica y Recomendaciones



\## 🚀 Configuración del Backend



1\. Navegar a la carpeta backend:

```bash

cd backend

```



2\. Instalar dependencias:

```bash

npm install

```



3\. Configurar variables de entorno (crear archivo `.env` basado en `.env.example`)



4\. Iniciar el servidor:

```bash

npm run dev

```



\## 👨‍💻 Autor



Andrés - CETI Colomos



\## 📄 Licencia



Este proyecto es privado y está en desarrollo.

```



\#### \*\*Paso 3.4:\*\* Guardar y cerrar



---



\### \*\*ARCHIVO 4: backend\\.env.example\*\*



\#### \*\*Paso 4.1:\*\* Navegar a backend

\- `C:\\Users\\andre.DESKTOP-FK64I3F\\OneDrive\\Documentos\\MisProyectos\\Hermes\\backend`



\#### \*\*Paso 4.2:\*\* Crear archivo

\- \*\*Clic derecho\*\* → \*\*Nuevo\*\* → \*\*Documento de texto\*\*

\- Renombrar a: `.env.example`

\- Confirmar



\#### \*\*Paso 4.3:\*\* Abrir y pegar:

```

\# Configuración del servidor

PORT=3000

NODE\_ENV=development



\# Configuración de la base de datos MariaDB

DB\_HOST=localhost

DB\_PORT=3306

DB\_NAME=hermes\_db

DB\_USER=root

DB\_PASSWORD=



\# Clave secreta para JWT

JWT\_SECRET=cambia\_esto\_por\_una\_clave\_segura



\# Configuración de Firebase (opcional)

FIREBASE\_PROJECT\_ID=

FIREBASE\_PRIVATE\_KEY=

FIREBASE\_CLIENT\_EMAIL=

```



\#### \*\*Paso 4.4:\*\* Guardar y cerrar



---



\## ✅ VERIFICACIÓN



Deberías tener esta estructura:

```

Hermes/

├── .gitignore                    ✅

├── README.md                     ✅

└── backend/

&nbsp;   ├── .gitignore                ✅

&nbsp;   ├── .env.example              ✅

&nbsp;   ├── .env                      (ya existía)

&nbsp;   ├── server.js

&nbsp;   ├── package.json

&nbsp;   └── ... (resto de archivos)

