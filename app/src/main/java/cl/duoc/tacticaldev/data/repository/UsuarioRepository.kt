package cl.duoc.tacticaldev.data.repository

import cl.duoc.tacticaldev.data.local.dao.UsuarioDao
import cl.duoc.tacticaldev.data.local.entity.UsuarioEntity
import cl.duoc.tacticaldev.utils.Seguridad

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    /** Devuelve el usuario si el correo existe y la clave coincide; si no, null. */
    suspend fun autenticar(correo: String, clave: String): UsuarioEntity? {
        val usuario = usuarioDao.buscarPorCorreo(correo.trim().lowercase()) ?: return null
        return usuario.takeIf { it.claveHash == Seguridad.sha256(clave) }
    }
    suspend fun registrar(
        nombre: String,
        correo: String,
        clave: String,
        rol: String,
        cargo: String
    ): Result<Long> {
        val correoLimpio = correo.trim().lowercase()
        val existente = usuarioDao.buscarPorCorreo(correoLimpio)
        if (existente != null) {
            return Result.failure(Exception("Ya existe una cuenta con este correo"))
        }
        val nuevo = UsuarioEntity(
            nombre = nombre.trim(),
            correo = correoLimpio,
            claveHash = Seguridad.sha256(clave),
            rol = rol,
            cargo = cargo.trim()
        )
        val id = usuarioDao.insertar(nuevo)
        return Result.success(id)
    }
}
