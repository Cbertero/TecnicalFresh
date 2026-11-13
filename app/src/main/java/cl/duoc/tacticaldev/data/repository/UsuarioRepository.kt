package cl.duoc.tacticaldev.data.repository

import cl.duoc.tacticaldev.data.local.dao.UsuarioDao
import cl.duoc.tacticaldev.data.local.entity.UsuarioEntity
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
            val remotos = RetrofitInstance.api.getUsuarios("OPERARIO")
            _operariosFlow.value = remotos
            runCatching { usuarioDao?.insertarTodos(remotos) }
            remotos
        } catch (e: Exception) {
            val locales = runCatching { usuarioDao?.observarOperarios()?.firstOrNull() }.getOrNull().orEmpty()
            if (locales.isNotEmpty()) {
                _operariosFlow.value = locales
            }
            _operariosFlow.value
        }
    }

    // valida el correo y la clave encriptada en el microservicio
    suspend fun autenticar(correo: String, clave: String): UsuarioEntity? {
        val correoLimpio = correo.trim().lowercase()
        val claveHash = Seguridad.sha256(clave)
        return try {
            val loginData = UsuarioEntity(
                id = 0,
                nombre = "",
                correo = correoLimpio,
                claveHash = claveHash,
                rol = "",
                cargo = ""
            )
            val usuario = RetrofitInstance.api.loginUsuario(loginData)
            runCatching { usuarioDao?.insertar(usuario) }
            usuario
        } catch (e: HttpException) {
            if (e.code() == 401) {
                null
            } else {
                val local = usuarioDao?.buscarPorCorreo(correoLimpio)
                local?.takeIf { it.claveHash == claveHash }
            }
        } catch (e: Exception) {
            val local = usuarioDao?.buscarPorCorreo(correoLimpio)
            local?.takeIf { it.claveHash == claveHash }
        }
    }

    // registra el usuario en la base de datos mysql por retrofit
    suspend fun registrar(
        nombre: String,
        correo: String,
        clave: String,
        rol: String,
        cargo: String
    ): Result<Long> {
        val correoLimpio = correo.trim().lowercase()
        val nuevo = UsuarioEntity(
            id = 0,
            nombre = nombre.trim(),
            correo = correoLimpio,
            claveHash = Seguridad.sha256(clave),
            rol = rol,
            cargo = cargo.trim()
        )
        return try {
            val creado = RetrofitInstance.api.registrarUsuario(nuevo)
            runCatching { usuarioDao?.insertar(creado) }
            refrescarOperarios()
            Result.success(creado.id)
        } catch (e: HttpException) {
            if (e.code() == 409) {
                Result.failure(Exception("Ya existe una cuenta con este correo"))
            } else {
                registrarEnLocal(nuevo)
            }
        } catch (e: Exception) {
            registrarEnLocal(nuevo)
        }
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
