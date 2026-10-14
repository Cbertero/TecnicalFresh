# Technical Fresh · Trazabilidad y Verificación de Visitas en Terreno

Proyecto semestral de **DSY1105 Desarrollo de Aplicaciones Móviles** (Duoc UC · Evaluación Parcial 2).

Aplicación móvil Android nativa desarrollada para resolver el problema de trazabilidad en la mantención y revisión de equipos de refrigeración (vitrinas freezer y conservadoras) en puntos de venta a nivel nacional. La solución reemplaza el uso informal de WhatsApp, Excel y papel, mitigando las *"visitas fantasma"* mediante verificación física con sensores nativos (GPS, Cámara y ML Kit Barcode), asegurando la continuidad de la cadena de frío y facilitando la gestión centralizada de supervisión.

---

## 👥 Equipo de Desarrollo

| Integrante | Rol en el Proyecto |
| :--- | :--- |
| **Claudio Bertero** | Integrante A · Arquitectura, Modelos, Base de Datos Room y Repositorios |
| **Felipe Durán** | Integrante B · UI/UX Jetpack Compose, Navegación, Dashboard, Registro y Alertas |

---

## 🚀 Funcionalidades Implementadas

### 1. Autenticación y Registro Autónomo
- **Registro de usuarios:** Formulario reactivo para enrolar nuevos operarios y supervisores con selección de especialidad (Técnico, Vendedora, Transportista).
- **Seguridad:** Cifrado seguro de credenciales con hashing SHA-256 almacenado localmente en SQLite/Room.
- **Persistencia de sesión:** Almacenamiento seguro del estado con Jetpack DataStore Preferences (si se cierra la app, se mantiene la sesión activa).

### 2. Panel de Control (Dashboards Hub)
- **Dashboard Operario:** Saludo personalizado por cargo, accesos directos en cuadrícula (Vitrinas, Mis Visitas, Escanear QR y Mi Perfil).
- **Dashboard Supervisor:** Acceso a módulos de Validación de Visitas en Terreno, Administración de Activos/Locales, Gestión de Equipo y Perfil.
- **Seguridad en salida:** Diálogo de confirmación interactivo (`AlertDialog`) antes de cerrar sesión para evitar toques accidentales.

### 3. Verificación en Terreno y Detección Temprana de Fallas
- **Escáner QR integrado:** Reconocimiento instantáneo del código pegado en cada vitrina mediante Google ML Kit Barcode Scanning para abrir directamente su ficha.
- **Geolocalización GPS:** Verificación automática de la presencia física real calculando la distancia en metros entre el dispositivo y las coordenadas registradas del local.
- **Evidencia fotográfica:** Captura de foto obligatoria en tiempo real con la cámara del dispositivo mediante `FileProvider` (prevención de fraudes).
- **Control de cadena de frío:** Detección de temperatura crítica con alertas visuales destacadas ante posibles fallas de motor o pérdidas de frío.

### 4. Trazabilidad y Supervisión
- **Bandeja de revisión:** Filtros por estado (Pendientes, Validadas, Rechazadas).
- **Aprobación o rechazo fundado:** El supervisor puede aprobar la visita o rechazarla ingresando obligatoriamente el motivo de rechazo.
- **Historial completo:** Consulta de visitas realizadas por operario y trazabilidad histórica acumulada por cada vitrina.

### 5. Administración
- Alta de nuevos locales/puntos de venta con captura de ubicación GPS actual.
- Registro de nuevos activos frigoríficos asociados a los puntos de venta.

### 6. Experiencia de Usuario y Animaciones
- **Feedback animado prémium:** Banners flotantes superiores con animación elástica de entrada y rebote (`Spring`) al validar, rechazar o crear registros.
- **Diseño Adaptativo (Window Size Classes):** Adaptación declarativa para pantallas Compactas (celulares), Medianas (tablets) y Expandidas.
- **Modo Oscuro Glacial (Arctic Cyan):** Interfaz estilizada de alto contraste inspirada en la cadena de frío.

---

## 🛠️ Tecnologías y Estándares Utilizados

- **Lenguaje:** Kotlin
- **Interfaz Declarativa:** Jetpack Compose con Material Design 3
- **Patrón Arquitectónico:** MVVM (Model-View-ViewModel) con StateFlow y UDF (Unidirectional Data Flow)
- **Persistencia Local:** Room Database (SQLite) + Jetpack DataStore Preferences
- **Hardware y Recursos Nativos:** Google Play Services Location (`FusedLocationProviderClient`), Google ML Kit Barcode Scanning y Cámara (`TakePicture` + `FileProvider`)
- **Adaptabilidad:** Android Window Size Classes (Guía 9)
- **Animaciones:** Jetpack Compose Animation (`Animatable`, `AnimatedVisibility`, `Spring`)
- **Control de Versiones:** Git y GitHub siguiendo metodología GitFlow (`main`, `develop`, ramas por tarea)

---

## 🔑 Usuarios de Prueba

Todos los usuarios preconfigurados en la base de datos utilizan la clave: **`Clave123`**

| Perfil / Cargo | Correo Electrónico | Función Principal |
| :--- | :--- | :--- |
| **Técnico de mantenimiento** | `tecnico@demo.cl` | Registro de mantenciones, lectura QR y temperatura |
| **Vendedora** | `vendedor@demo.cl` | Visitas comerciales y estado de equipos |
| **Transportista** | `transportista@demo.cl` | Entregas, retiros y evidencia en terreno |
| **Supervisora de operaciones** | `supervisor@demo.cl` | Validación/rechazo de visitas y administración |

> *También puedes registrar nuevos usuarios usando la opción **"¿No tienes cuenta? Regístrate aquí"** en el Login.*

---

## 📲 Pasos para Ejecutar el Proyecto

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/Cbertero/TecnicalFresh.git