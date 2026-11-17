package cl.duoc.tacticaldev.data.repository

import cl.duoc.tacticaldev.data.local.dao.UsuarioDao
import cl.duoc.tacticaldev.data.local.entity.UsuarioEntity
import cl.duoc.tacticaldev.data.model.LoginRequest
import cl.duoc.tacticaldev.data.model.RegistroRequest
import cl.duoc.tacticaldev.data.model.aEntidad
import cl.duoc.tacticaldev.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onStart
import retrofit2.HttpException
import kotlin.coroutines.cancellation.CancellationException

/** No se pudo contactar al microservicio: no hay internet o el servidor no responde. */
class ServidorNoDisponibleException :
    Exception("Sin conexión con el servidor. Necesitas internet para continuar")

class UsuarioRepository(private val usuarioDao: UsuarioDao? = null) {

    /** true si el microservicio responde; false si no hay conexión o falla. */
    suspend fun servidorDisponible(): Boolean = try {
        RetrofitInstance.api.getPuntosVenta()
        true
    } catch (e: Exception) {
        false
    }

    private val _operariosFlow = MutableStateFlow<List<UsuarioEntity>>(emptyList())

    fun observarOperarios(): Flow<List<UsuarioEntity>> = _operariosFlow.onStart {
        refrescarOperarios()
    }

    suspend fun refrescarOperarios(): List<UsuarioEntity> {
        return try {
            val remotos = RetrofitInstance.api.getUsuarios("OPERARIO").map { it.aEntidad() }
            _operariosFlow.value = remotos
            // No se guardan en Room: el servidor ya no envía el hash y REPLACE
            // borraría el hash local de quienes iniciaron sesión en este teléfono.
            remotos
        } catch (e: Exception) {
            val locales = runCatching { usuarioDao?.observarOperarios()?.firstOrNull() }.getOrNull().orEmpty()
            if (locales.isNotEmpty()) {
                _operariosFlow.value = locales
            }
            _operariosFlow.value
        }
    }

    // El login SIEMPRE se valida en el microservicio (BCrypt en MySQL).
    // En el teléfono no se guarda ninguna clave: sin servidor no se puede entrar.
    suspend fun autenticar(correo: String, clave: String): UsuarioEntity? {
        val correoLimpio = correo.trim().lowercase()
        return try {
            RetrofitInstance.api.loginUsuario(LoginRequest(correoLimpio, clave)).aEntidad()
        } catch (e: HttpException) {
            if (e.code() == 401) null else throw ServidorNoDisponibleException()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw ServidorNoDisponibleException()
        }
    }

    // Registra el usuario en MySQL; el servidor guarda la clave con BCrypt
    suspend fun registrar(
        nombre: String,
        correo: String,
        clave: String,
        rol: String,
        cargo: String
    ): Result<Long> {
        val correoLimpio = correo.trim().lowercase()
        return try {
            val creado = RetrofitInstance.api
                .registrarUsuario(RegistroRequest(nombre.trim(), correoLimpio, clave, rol, cargo.trim()))
                .aEntidad()
            refrescarOperarios()
            Result.success(creado.id)
        } catch (e: HttpException) {
            if (e.code() == 409) {
                Result.failure(Exception("Ya existe una cuenta con este correo"))
            } else {
                Result.failure(ServidorNoDisponibleException())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(ServidorNoDisponibleException())
        }
    }
}