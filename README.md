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

### 7. Integración con Microservicio REST (API en la nube)
- **Asignación de locales a trabajadores:** la pantalla *Asignar Locales (API)* consume el microservicio `microservicioTecnico` (Spring Boot + MySQL) desplegado en **Railway**.
- **Operaciones disponibles:** listar, crear y eliminar asignaciones mediante Retrofit sobre HTTPS.
- **Modo offline con sincronización automática:** si no hay internet, la asignación se guarda en el teléfono con estado *"Pendiente de Internet"* y se sube sola al servidor cuando vuelve la conexión.
- **Indicador de estado:** cada registro muestra si está *Sincronizado en Servidor (MySQL)* o *Guardado en Local*.

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
- **Consumo de API REST:** Retrofit 2 + Gson + Kotlin Coroutines
- **Backend:** Spring Boot + Spring Data JPA + MySQL, desplegado en Railway
- **Pruebas unitarias:** Kotest + MockK + kotlinx-coroutines-test, con cobertura medida por JaCoCo

---

## 🌐 Arquitectura y API

```
App Android (Retrofit) ──HTTPS──▶ Microservicio Spring Boot (Railway) ──▶ MySQL (Railway)
```

- **Repositorio del microservicio:** [Fduran32/microservicoTecnicalFresh](https://github.com/Fduran32/microservicoTecnicalFresh)
- **URL base:** `https://microservicotecnicalfresh-production-c2a9.up.railway.app/`

| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `GET` | `/api/v1/tecnicos` | Lista las asignaciones |
| `POST` | `/api/v1/tecnicos` | Crea una asignación (`nombre`, `cargo`, `localAsignado`) |
| `DELETE` | `/api/v1/tecnicos/{id}` | Elimina una asignación |

**Persistencia actual:**

| Dato | Dónde se guarda |
| :--- | :--- |
| Asignaciones de locales | MySQL en Railway (con caché y cola offline en el teléfono) |
| Usuarios, visitas, activos y locales | Room (SQLite) local en cada dispositivo |

> ⚠️ **Limitación conocida:** los usuarios se almacenan solo en el dispositivo (clave con hash SHA-256). Una mejora futura es moverlos al servidor con hashing BCrypt para permitir el inicio de sesión desde cualquier teléfono.

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
   ```
2. **Abrir el proyecto** en Android Studio y esperar a que Gradle sincronice las dependencias.
3. **Ejecutar la app** en un emulador o teléfono con Android 8.0 (API 26) o superior.
4. **Iniciar sesión** con alguno de los usuarios de prueba (clave `Clave123`).

> La app ya apunta al microservicio en Railway, no es necesario levantarlo localmente.
> Para probar contra un microservicio local desde el emulador, cambia `BASE_URL` en `RetrofitInstance.kt` a `http://10.0.2.2:8081/`.

---

## 🧪 Pruebas Unitarias

Las pruebas están escritas con **Kotest** (estilo `FunSpec`) y **MockK**, y se ejecutan en la JVM, sin emulador.

| Capa | Qué se prueba | Técnica |
| :--- | :--- | :--- |
| Validadores, modelos, utilidades y navegación | Reglas de negocio y funciones puras | Entrada → salida esperada |
| Repositorios de Room | Que deleguen correctamente en los DAO | Mocks de MockK + `coVerify` |
| `TecnicoRepository` (API) | Modo online, modo offline y sincronización de pendientes | `FakeSharedPreferences` + `mockkObject(RetrofitInstance)` |
| ViewModels (12) | Validaciones, mensajes de estado, guardado y navegación | Mocks + `Dispatchers.Main` de prueba |

**Ejecutar las pruebas:**
```bash
./gradlew testDebugUnitTest
```

**Generar el reporte de cobertura (JaCoCo):**
```bash
./gradlew jacocoTestReport
```
El reporte queda en `app/build/reports/jacoco/jacocoTestReport/html/index.html`.

**Resultados actuales:** 172 pruebas, 0 fallos y **99% de cobertura de líneas**.

> La cobertura se mide sobre la lógica de la app (ViewModels, repositorios, validaciones, modelos y utilidades). Se excluyen las pantallas Compose y los envoltorios del framework Android (GPS, cámara, DataStore, DAO de Room), que requieren pruebas instrumentadas en un emulador (`androidTest`).
