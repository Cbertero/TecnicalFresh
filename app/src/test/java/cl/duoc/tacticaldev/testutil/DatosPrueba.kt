package cl.duoc.tacticaldev.testutil

import cl.duoc.tacticaldev.data.local.entity.ActivoEntity
import cl.duoc.tacticaldev.data.local.entity.UsuarioEntity
import cl.duoc.tacticaldev.data.local.entity.VisitaPendienteEntity
import cl.duoc.tacticaldev.data.preferences.Sesion
import cl.duoc.tacticaldev.model.ActivoConPunto
import cl.duoc.tacticaldev.model.EstadoEquipo
import cl.duoc.tacticaldev.model.EstadoValidacion
import cl.duoc.tacticaldev.model.Rol
import cl.duoc.tacticaldev.model.TipoActivo
import cl.duoc.tacticaldev.model.TipoVisita
import cl.duoc.tacticaldev.model.VisitaDetalle

/**
 * "Fábrica" de objetos de prueba. Así cada prueba crea datos con una sola línea
 * y solo cambia el campo que le interesa (ej: activoConPunto(puntoVentaNombre = "X")).
 */
object DatosPrueba {

    fun activoConPunto(
        id: Long = 1,
        codigo: String = "TDV-VF-001",
        puntoVentaNombre: String = "Minimarket El Alba",
        comuna: String = "Cerrillos",
        latitud: Double = -33.5160,
        longitud: Double = -70.7170
    ) = ActivoConPunto(
        id = id,
        codigo = codigo,
        tipo = TipoActivo.VITRINA_FREEZER.name,
        marca = "FrioMax",
        modelo = "VF-300",
        puntoVentaId = 1,
        puntoVentaNombre = puntoVentaNombre,
        direccion = "Av. Ficticia 1234",
        comuna = comuna,
        latitud = latitud,
        longitud = longitud,
        ultimaVisita = null
    )

    fun activoEntity(id: Long = 1, codigo: String = "TDV-VF-001") = ActivoEntity(
        id = id,
        codigo = codigo,
        tipo = TipoActivo.VITRINA_FREEZER.name,
        marca = "FrioMax",
        modelo = "VF-300",
        puntoVentaId = 1
    )

    fun visitaDetalle(
        id: Long = 10,
        activoCodigo: String = "TDV-VF-001",
        estadoValidacion: String = EstadoValidacion.PENDIENTE.name
    ) = VisitaDetalle(
        id = id,
        activoId = 1,
        activoCodigo = activoCodigo,
        activoTipo = TipoActivo.VITRINA_FREEZER.name,
        puntoVentaNombre = "Minimarket El Alba",
        usuarioId = 1,
        usuarioNombre = "Tomás Rivera",
        fechaHora = 1_000L,
        tipoVisita = TipoVisita.MANTENCION.name,
        estadoEquipo = EstadoEquipo.OPERATIVO.name,
        temperatura = -18.0,
        observaciones = "Todo en orden",
        fotoUri = "content://foto.jpg",
        distanciaMetros = 12.0,
        ubicacionVerificada = true,
        estadoValidacion = estadoValidacion,
        motivoRechazo = null
    )

    fun usuario(
        id: Long = 1,
        correo: String = "tecnico@demo.cl",
        claveHash: String = "hash",
        rol: String = Rol.OPERARIO.name
    ) = UsuarioEntity(
        id = id,
        nombre = "Tomás Rivera",
        correo = correo,
        claveHash = claveHash,
        rol = rol,
        cargo = "Técnico de mantenimiento"
    )

    fun sesion(
        usuarioId: Long = 1,
        nombre: String = "Tomás Rivera",
        rol: Rol = Rol.OPERARIO,
        cargo: String = "Técnico de mantenimiento"
    ) = Sesion(usuarioId, nombre, rol, cargo)

    /** Visita guardada sin internet, esperando enviarse al microservicio. */
    fun visitaPendiente(id: Long = 1, usuarioId: Long = 1) = VisitaPendienteEntity(
        id = id, activoId = 1, usuarioId = usuarioId, fechaHora = 1_000L,
        tipoVisita = "MANTENCION", estadoEquipo = "OPERATIVO", temperatura = -18.5,
        observaciones = "Sin señal en la bodega", fotoBase64 = "data:image/jpeg;base64,AAA",
        latitud = -33.5, longitud = -70.7, distanciaMetros = 12.0, ubicacionVerificada = true
    )
}
