package cl.duoc.tacticaldev.data.repository

import cl.duoc.tacticaldev.data.local.dao.ActivoDao
import cl.duoc.tacticaldev.data.local.dao.PuntoVentaDao
import cl.duoc.tacticaldev.data.local.entity.ActivoEntity
import cl.duoc.tacticaldev.data.local.entity.PuntoVentaEntity
import cl.duoc.tacticaldev.data.remote.RetrofitInstance
import cl.duoc.tacticaldev.model.ActivoConPunto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onStart

class ActivoRepository(
    private val activoDao: ActivoDao? = null,
    private val puntoVentaDao: PuntoVentaDao? = null
) {
    private val _puntosFlow = MutableStateFlow<List<PuntoVentaEntity>>(emptyList())
    private val _activosFlow = MutableStateFlow<List<ActivoConPunto>>(emptyList())

    fun observarActivos(): Flow<List<ActivoConPunto>> = _activosFlow.onStart {
        refrescarDatos()
    }

    fun observarPuntosVenta(): Flow<List<PuntoVentaEntity>> = _puntosFlow.onStart {
        refrescarDatos()
    }

    // trae los locales y activos desde el microservicio
    suspend fun refrescarDatos() {
        try {
            val puntos = RetrofitInstance.api.getPuntosVenta().sortedBy { it.nombre }
            val activos = RetrofitInstance.api.getActivos()
            _puntosFlow.value = puntos
            _activosFlow.value = armarActivosConPunto(activos, puntos)

            runCatching {
                puntoVentaDao?.insertarTodos(puntos)
                activoDao?.insertarTodos(activos)
            }
        } catch (e: Exception) {
            val puntosLocales = runCatching { puntoVentaDao?.observarTodos()?.firstOrNull() }.getOrNull().orEmpty()
            val activosLocales = runCatching { activoDao?.observarActivosConPunto()?.firstOrNull() }.getOrNull().orEmpty()
            if (puntosLocales.isNotEmpty()) _puntosFlow.value = puntosLocales
            if (activosLocales.isNotEmpty()) _activosFlow.value = activosLocales
        }
    }

    suspend fun obtenerActivo(id: Long): ActivoConPunto? {
        if (_activosFlow.value.isEmpty()) {
            refrescarDatos()
        }
        return _activosFlow.value.find { it.id == id }
            ?: runCatching { activoDao?.buscarActivoConPunto(id) }.getOrNull()
    }

    suspend fun buscarPorCodigo(codigo: String): ActivoEntity? {
        val codigoLimpio = codigo.trim().uppercase()
        return try {
            RetrofitInstance.api.getActivoPorCodigo(codigoLimpio)
        } catch (e: Exception) {
            runCatching { activoDao?.buscarPorCodigo(codigoLimpio) }.getOrNull()
        }
    }

    suspend fun registrarPuntoVenta(puntoVenta: PuntoVentaEntity): Long {
        return try {
            val guardado = RetrofitInstance.api.guardarPuntoVenta(puntoVenta)
            runCatching { puntoVentaDao?.insertar(guardado) }
            refrescarDatos()
            guardado.id
        } catch (e: Exception) {
            val id = puntoVentaDao?.insertar(puntoVenta) ?: 0L
            refrescarDatos()
            id
        }
    }

    suspend fun registrarActivo(activo: ActivoEntity): Long {
        val limpio = activo.copy(codigo = activo.codigo.trim().uppercase())
        return try {
            val guardado = RetrofitInstance.api.guardarActivo(limpio)
            runCatching { activoDao?.insertar(guardado) }
            refrescarDatos()
            guardado.id
        } catch (e: Exception) {
            val id = activoDao?.insertar(limpio) ?: 0L
            refrescarDatos()
            id
        }
    }

    // junta los datos del activo con su punto de venta
    private fun armarActivosConPunto(
        activos: List<ActivoEntity>,
        puntos: List<PuntoVentaEntity>
    ): List<ActivoConPunto> {
        val mapaPuntos = puntos.associateBy { it.id }
        return activos.mapNotNull { a ->
            val p = mapaPuntos[a.puntoVentaId] ?: return@mapNotNull null
            ActivoConPunto(
                id = a.id,
                codigo = a.codigo,
                tipo = a.tipo,
                marca = a.marca,
                modelo = a.modelo,
                puntoVentaId = a.puntoVentaId,
                puntoVentaNombre = p.nombre,
                direccion = p.direccion,
                comuna = p.comuna,
                latitud = p.latitud,
                longitud = p.longitud,
                ultimaVisita = null
            )
        }.sortedWith(compareBy({ it.puntoVentaNombre }, { it.codigo }))
    }
}
