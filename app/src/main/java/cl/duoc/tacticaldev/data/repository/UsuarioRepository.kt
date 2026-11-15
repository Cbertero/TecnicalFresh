package cl.duoc.tacticaldev.data.repository

import cl.duoc.tacticaldev.data.local.dao.UsuarioDao
import cl.duoc.tacticaldev.data.local.entity.UsuarioEntity
import cl.duoc.tacticaldev.data.model.LoginRequest
import cl.duoc.tacticaldev.data.model.RegistroRequest
import cl.duoc.tacticaldev.data.model.aEntidad
import cl.duoc.tacticaldev.data.remote.RetrofitInstance
import cl.duoc.tacticaldev.utils.Seguridad
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onStart
import retrofit2.HttpException

class UsuarioRepository(private val usuarioDao: UsuarioDao? = null) {

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

    // Envía la clave por HTTPS; el servidor la compara con BCrypt
    suspend fun autenticar(correo: String, clave: String): UsuarioEntity? {
        val correoLimpio = correo.trim().lowercase()
        val claveHash = Seguridad.sha256(clave) // solo para el respaldo sin internet
        return try {
            val usuario = RetrofitInstance.api
                .loginUsuario(LoginRequest(correoLimpio, clave))
                .aEntidad(claveHashLocal = claveHash)
            runCatching { usuarioDao?.insertar(usuario) }
            usuario
        } catch (e: HttpException) {
            if (e.code() == 401) {
                null
            } else {
                loginLocal(correoLimpio, claveHash)
            }
        } catch (e: Exception) {
            loginLocal(correoLimpio, claveHash)
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
        val claveHash = Seguridad.sha256(clave)
        return try {
            val creado = RetrofitInstance.api
                .registrarUsuario(RegistroRequest(nombre.trim(), correoLimpio, clave, rol, cargo.trim()))
                .aEntidad(claveHashLocal = claveHash)
            runCatching { usuarioDao?.insertar(creado) }
            refrescarOperarios()
            Result.success(creado.id)
        } catch (e: HttpException) {
            if (e.code() == 409) {
                Result.failure(Exception("Ya existe una cuenta con este correo"))
            } else {
                registrarEnLocal(UsuarioEntity(0, nombre.trim(), correoLimpio, claveHash, rol, cargo.trim()))
            }
        } catch (e: Exception) {
            registrarEnLocal(UsuarioEntity(0, nombre.trim(), correoLimpio, claveHash, rol, cargo.trim()))
        }
    }

    private suspend fun loginLocal(correo: String, claveHash: String): UsuarioEntity? {
        val local = usuarioDao?.buscarPorCorreo(correo)
        return local?.takeIf { it.claveHash.isNotEmpty() && it.claveHash == claveHash }
    }

    private suspend fun registrarEnLocal(nuevo: UsuarioEntity): Result<Long> {
        val dao = usuarioDao ?: return Result.failure(Exception("Error de conexion con el servidor"))
        val existente = dao.buscarPorCorreo(nuevo.correo)
        if (existente != null) {
            return Result.failure(Exception("Ya existe una cuenta con este correo"))
        }
        val id = dao.insertar(nuevo)
        refrescarOperarios()
        return Result.success(id)
    }
}