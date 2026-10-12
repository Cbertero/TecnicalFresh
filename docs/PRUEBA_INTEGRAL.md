# Prueba integral (T-19)

Pauta para probar la app completa en un **dispositivo físico** (recomendado para cámara, GPS y QR)
o en el emulador. Marquen cada caso y anoten el resultado. Si algo falla, se corrige en la rama
`fix/T-19-prueba-integral`.

Dispositivo usado: ____________________ · Android: ______ · Fecha: __________ · Responsable: Claudio

## 1. Sesión y navegación (IE 2.1.1 · IE 2.1.2)

| # | Caso | Resultado esperado | OK |
|---|------|--------------------|----|
| 1 | Ingresar con correo sin "@" | Campo en rojo con ícono y mensaje "El formato del correo no es válido" | [ ] |
| 2 | Ingresar con clave incorrecta | Mensaje "Correo o clave incorrectos" y el formulario se sacude | [ ] |
| 3 | Ingresar como `tecnico@demo.cl` / `Clave123` | Abre "Hola, Tomás Rivera" con la lista de activos | [ ] |
| 4 | Cerrar la app y volver a abrirla | Sigue con la sesión iniciada (DataStore) | [ ] |
| 5 | Cambiar entre Activos y Mis visitas | La barra inferior marca la pestaña activa | [ ] |
| 6 | Girar el teléfono o usar tablet | Se muestra riel lateral y grilla de 2-3 columnas | [ ] |
| 7 | Ingresar como `supervisor@demo.cl` | Abre la vista de visitas con pestañas Visitas / Administración | [ ] |

## 2. Registro de visita (IE 2.1.2 · IE 2.2.1 · IE 2.3.1 · IE 2.4.1)

| # | Caso | Resultado esperado | OK |
|---|------|--------------------|----|
| 8 | Presionar "Guardar visita" con el formulario vacío | Todos los campos obligatorios en rojo y el botón se sacude | [ ] |
| 9 | Temperatura "25" | Error "Debe estar entre -30 y 15 °C" | [ ] |
| 10 | Estado "Con falla" y observaciones vacías | Aviso animado y error en observaciones | [ ] |
| 11 | Tomar foto y aceptar | La foto se ve en la tarjeta de evidencia | [ ] |
| 12 | Abrir la cámara y cancelar | No se cae la app y no queda foto | [ ] |
| 13 | Denegar permiso de cámara | Mensaje "Sin permiso de cámara no se puede registrar la evidencia" | [ ] |
| 14 | Obtener ubicación lejos del local | Insignia ámbar "Fuera de rango" con la distancia | [ ] |
| 15 | Obtener ubicación con GPS apagado | Tras 15 s máximo aparece "No se pudo obtener la ubicación" | [ ] |
| 16 | Guardar con todo válido | Pantalla de confirmación animada con el resumen y la foto | [ ] |
| 17 | Volver al inicio | La tarjeta del activo muestra la nueva "Última visita" | [ ] |
| 18 | Escanear el QR `docs/qr/TDV-VF-001.png` | Abre directo el formulario del activo TDV-VF-001 | [ ] |
| 19 | Cerrar la app sin internet y reabrir | La visita sigue guardada (Room, sin conexión) | [ ] |

## 3. Historial y supervisor (IE 2.1.1 · IE 2.2.2 · IE 2.3.1)

| # | Caso | Resultado esperado | OK |
|---|------|--------------------|----|
| 20 | Abrir "Historial" de un activo | Visitas ordenadas de la más nueva a la más antigua | [ ] |
| 21 | Abrir "Mis visitas" | Solo aparecen las visitas del usuario conectado | [ ] |
| 22 | Supervisor valida una visita | La tarjeta sale de "Pendientes" con animación y aparece un aviso | [ ] |
| 23 | Rechazar con motivo de 5 letras | Error "El motivo debe tener al menos 10 caracteres" | [ ] |
| 24 | Rechazar con motivo válido | La visita pasa a "Rechazadas" y el operario ve el motivo en Mis visitas | [ ] |
| 25 | Registrar punto de venta con "Usar mi ubicación actual" | Se completan latitud y longitud, y se guarda | [ ] |
| 26 | Registrar activo con un código repetido | Error "Ya existe un activo con este código" | [ ] |

## 4. Pruebas unitarias (IE 2.2.1)

```bash
./gradlew test
```

| # | Caso | Resultado esperado | OK |
|---|------|--------------------|----|
| 27 | `ValidadoresTest` | Todas las pruebas en verde | [ ] |

## Correcciones aplicadas en T-19

- La cámara perdía el URI de la foto si Android destruía la actividad mientras estaba abierta: ahora se guarda con `rememberSaveable`.
- Al cancelar la cámara quedaba un archivo vacío en `files/evidencias`: ahora se elimina.
- Con el GPS apagado la búsqueda de ubicación podía quedar esperando sin fin: ahora tiene un máximo de 15 segundos.
