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
}
