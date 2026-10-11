package cl.duoc.tacticaldev.utils

import java.security.MessageDigest

/** Utilidades de seguridad básicas para el MVP. */
object Seguridad {
    /** Devuelve el hash SHA-256 en hexadecimal; así la clave nunca se guarda en texto plano. */
    fun sha256(texto: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(texto.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
