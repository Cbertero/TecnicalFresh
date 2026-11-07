package cl.duoc.tacticaldev.data.repository

import android.content.Context
import cl.duoc.tacticaldev.data.model.Tecnico
import cl.duoc.tacticaldev.data.remote.RetrofitInstance
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TecnicoRepository(context: Context) {

    private val prefs = context.getSharedPreferences("tecnicos_offline_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _asignacionesFlow = MutableStateFlow<List<Tecnico>>(leerTodoLocal())
    val asignacionesFlow: StateFlow<List<Tecnico>> = _asignacionesFlow.asStateFlow()

    /**
     * Intenta sincronizar los pendientes con el servidor y descargar la lista actualizada.
     * Si no hay internet, devuelve la copia local + los pendientes sin fallar.
     */
    suspend fun geTecnicos(): List<Tecnico> {
        sincronizarPendientes()
        return try {
            val remotos = RetrofitInstance.api.getTecnicos().map {
                it.copy(localAsignado = it.localAsignado.orEmpty(), sincronizado = true)
            }
            guardarCacheServidor(remotos)
            val combinados = remotos + leerColaPendientes()
            _asignacionesFlow.value = combinados
            combinados
        } catch (e: Exception) {
            // Modo Offline: devolvemos lo que hay guardado en el teléfono
            val locales = leerTodoLocal()
            _asignacionesFlow.value = locales
            locales
        }
    }

    /**
     * Guarda una asignación de local. Si hay internet, va directo a MySQL.
     * Si no hay internet, se guarda en la cola local (sincronizado = false).
     */
    suspend fun GuardarTecnico(nombre: String, cargo: String, localAsignado: String): Tecnico {
        val nuevo = Tecnico(
            id = null,
            nombre = nombre,
            cargo = cargo,
            localAsignado = localAsignado,
            sincronizado = true
        )
        return try {
            val guardadoRemoto = RetrofitInstance.api.guardarTecnico(nuevo).copy(
                localAsignado = localAsignado,
                sincronizado = true
            )
            geTecnicos()
            guardadoRemoto
        } catch (e: Exception) {
            // Sin conexión: guardamos localmente con un ID temporal negativo
            val pendiente = nuevo.copy(
                id = -System.currentTimeMillis(),
                sincronizado = false
            )
            val colaActual = leerColaPendientes().toMutableList()
            colaActual.add(pendiente)
            guardarColaPendientes(colaActual)
            _asignacionesFlow.value = leerTodoLocal()
            pendiente
        }
    }

    /**
     * Elimina una asignación (del servidor si tiene ID positivo, o de la cola local si era offline).
     */
    suspend fun eliminarTecnico(id: Long) {
        if (id < 0) {
            val colaActual = leerColaPendientes().filterNot { it.id == id }
            guardarColaPendientes(colaActual)
            _asignacionesFlow.value = leerTodoLocal()
            return
        }
        try {
            RetrofitInstance.api.eliminarTecnico(id)
        } catch (_: Exception) {
            // Si está offline, lo quitamos también del caché local
        }
        val cacheActual = leerCacheServidor().filterNot { it.id == id }
        guardarCacheServidor(cacheActual)
        _asignacionesFlow.value = leerTodoLocal()
    }

    /**
     * Sube automáticamente a MySQL todos los registros creados cuando no había internet.
     */
    suspend fun sincronizarPendientes() {
        val pendientes = leerColaPendientes()
        if (pendientes.isEmpty()) return

        val aunPendientes = mutableListOf<Tecnico>()
        for (item in pendientes) {
            try {
                RetrofitInstance.api.guardarTecnico(
                    Tecnico(
                        id = null,
                        nombre = item.nombre,
                        cargo = item.cargo,
                        localAsignado = item.localAsignado.orEmpty()
                    )
                )
            } catch (e: Exception) {
                aunPendientes.add(item)
            }
        }
        guardarColaPendientes(aunPendientes)
    }

    private fun leerTodoLocal(): List<Tecnico> = leerCacheServidor() + leerColaPendientes()

    private fun leerCacheServidor(): List<Tecnico> {
        val json = prefs.getString(KEY_CACHE_SERVIDOR, null) ?: return emptyList()
        val type = object : TypeToken<List<Tecnico>>() {}.type
        return runCatching { gson.fromJson<List<Tecnico>>(json, type) }.getOrDefault(emptyList())
    }

    private fun guardarCacheServidor(lista: List<Tecnico>) {
        prefs.edit().putString(KEY_CACHE_SERVIDOR, gson.toJson(lista)).apply()
    }

    private fun leerColaPendientes(): List<Tecnico> {
        val json = prefs.getString(KEY_COLA_PENDIENTES, null) ?: return emptyList()
        val type = object : TypeToken<List<Tecnico>>() {}.type
        return runCatching { gson.fromJson<List<Tecnico>>(json, type) }.getOrDefault(emptyList())
    }

    private fun guardarColaPendientes(lista: List<Tecnico>) {
        prefs.edit().putString(KEY_COLA_PENDIENTES, gson.toJson(lista)).apply()
    }

    private companion object {
        const val KEY_CACHE_SERVIDOR = "cache_servidor"
        const val KEY_COLA_PENDIENTES = "cola_pendientes"
    }
}
