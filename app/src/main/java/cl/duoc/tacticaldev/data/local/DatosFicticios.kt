package cl.duoc.tacticaldev.data.local

import cl.duoc.tacticaldev.data.local.entity.ActivoEntity
import cl.duoc.tacticaldev.data.local.entity.PuntoVentaEntity
import cl.duoc.tacticaldev.data.local.entity.UsuarioEntity
import cl.duoc.tacticaldev.data.local.entity.VisitaEntity
import cl.duoc.tacticaldev.model.EstadoEquipo
import cl.duoc.tacticaldev.model.EstadoValidacion
import cl.duoc.tacticaldev.model.Rol
import cl.duoc.tacticaldev.model.TipoActivo
import cl.duoc.tacticaldev.model.TipoVisita
import cl.duoc.tacticaldev.utils.Seguridad

/**
 * Datos 100% ficticios para el MVP (el caso prohíbe usar datos reales).
 * Todos los usuarios tienen la clave "Clave123".
 */
object DatosFicticios {

    const val CLAVE_DEMO = "Clave123"
    private const val HORA = 60 * 60 * 1000L

    fun usuarios(): List<UsuarioEntity> {
        val hash = Seguridad.sha256(CLAVE_DEMO)
        return listOf(
            UsuarioEntity(1, "Tomás Rivera", "tecnico@demo.cl", hash, Rol.OPERARIO.name, "Técnico de mantenimiento"),
            UsuarioEntity(2, "Valentina Soto", "vendedor@demo.cl", hash, Rol.OPERARIO.name, "Vendedora"),
            UsuarioEntity(3, "Diego Muñoz", "transportista@demo.cl", hash, Rol.OPERARIO.name, "Transportista"),
            UsuarioEntity(4, "Carla Fuentes", "supervisor@demo.cl", hash, Rol.SUPERVISOR.name, "Supervisora de operaciones")
        )
    }

    fun puntosVenta(): List<PuntoVentaEntity> = listOf(
        PuntoVentaEntity(1, "Minimarket El Alba", "Av. Ficticia 1234", "Cerrillos", -33.5160, -70.7170),
        PuntoVentaEntity(2, "Almacén Los Aromos", "Pasaje Demo 456", "Maipú", -33.5105, -70.7580),
        PuntoVentaEntity(3, "Minimarket Las Rosas", "Calle Ejemplo 789", "Santiago", -33.4489, -70.6693),
        PuntoVentaEntity(4, "Botillería La Esquina", "Av. Prueba 321", "Estación Central", -33.4569, -70.6983)
    )

    fun activos(): List<ActivoEntity> = listOf(
        ActivoEntity(1, "TDV-VF-001", TipoActivo.VITRINA_FREEZER.name, "FrioMax", "VF-300", 1),
        ActivoEntity(2, "TDV-CS-001", TipoActivo.CONSERVADORA.name, "GlaciaPro", "CS-150", 1),
        ActivoEntity(3, "TDV-VF-002", TipoActivo.VITRINA_FREEZER.name, "FrioMax", "VF-450", 2),
        ActivoEntity(4, "TDV-CS-002", TipoActivo.CONSERVADORA.name, "GlaciaPro", "CS-200", 3),
        ActivoEntity(5, "TDV-VF-003", TipoActivo.VITRINA_FREEZER.name, "PolarLine", "PL-500", 3),
        ActivoEntity(6, "TDV-CS-003", TipoActivo.CONSERVADORA.name, "PolarLine", "PL-120", 4)
    )

    /** Algunas visitas de ejemplo para que el historial y el supervisor no partan vacíos. */
    fun visitas(ahora: Long = System.currentTimeMillis()): List<VisitaEntity> = listOf(
        VisitaEntity(
            activoId = 1, usuarioId = 1, fechaHora = ahora - 50 * HORA,
            tipoVisita = TipoVisita.MANTENCION.name, estadoEquipo = EstadoEquipo.OPERATIVO.name,
            temperatura = -18.0, observaciones = "Limpieza de condensador y revisión de sellos.",
            fotoUri = "", latitud = -33.5161, longitud = -70.7171, distanciaMetros = 14.0,
            ubicacionVerificada = true, estadoValidacion = EstadoValidacion.VALIDADA.name, supervisorId = 4
        ),
        VisitaEntity(
            activoId = 4, usuarioId = 2, fechaHora = ahora - 26 * HORA,
            tipoVisita = TipoVisita.VENTA.name, estadoEquipo = EstadoEquipo.CON_OBSERVACIONES.name,
            temperatura = -12.5, observaciones = "La puerta no cierra completamente, se acumula escarcha.",
            fotoUri = "", latitud = -33.4490, longitud = -70.6695, distanciaMetros = 21.0,
            ubicacionVerificada = true
        ),
        VisitaEntity(
            activoId = 6, usuarioId = 3, fechaHora = ahora - 3 * HORA,
            tipoVisita = TipoVisita.TRANSPORTE.name, estadoEquipo = EstadoEquipo.CON_FALLA.name,
            temperatura = 2.0, observaciones = "Equipo sin enfriar al momento de la entrega de producto.",
            fotoUri = "", latitud = -33.4700, longitud = -70.7100, distanciaMetros = 1850.0,
            ubicacionVerificada = false
        )
    )
}
