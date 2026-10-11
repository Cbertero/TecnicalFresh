package cl.duoc.tacticaldev.domain.validacion

import cl.duoc.tacticaldev.model.EstadoEquipo

/**
 * Reglas de validación centralizadas y desacopladas de la interfaz.
 * Cada función devuelve el mensaje de error o null si el valor es válido.
 * Al ser Kotlin puro se pueden probar con pruebas unitarias (T-22).
 */
object Validadores {

    private val REGEX_CORREO = Regex("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")
    private val REGEX_CODIGO_ACTIVO = Regex("^TDV-(VF|CS)-\\d{3}$")

    const val CLAVE_MIN = 6
    const val TEMPERATURA_MIN = -30.0
    const val TEMPERATURA_MAX = 15.0
    const val OBSERVACIONES_MIN = 10
    const val OBSERVACIONES_MAX = 500
    const val MOTIVO_MIN = 10

    fun correo(valor: String): String? = when {
        valor.isBlank() -> "Ingresa tu correo"
        !REGEX_CORREO.matches(valor.trim()) -> "El formato del correo no es válido"
        else -> null
    }

    fun clave(valor: String): String? = when {
        valor.isBlank() -> "Ingresa tu clave"
        valor.length < CLAVE_MIN -> "La clave debe tener al menos $CLAVE_MIN caracteres"
        else -> null
    }

    fun seleccion(valor: Any?, campo: String): String? =
        if (valor == null) "Selecciona $campo" else null

    fun temperatura(valor: String): String? {
        if (valor.isBlank()) return "Ingresa la temperatura medida"
        val numero = valor.replace(',', '.').toDoubleOrNull() ?: return "Ingresa un número válido"
        return if (numero < TEMPERATURA_MIN || numero > TEMPERATURA_MAX) {
            "Debe estar entre ${TEMPERATURA_MIN.toInt()} y ${TEMPERATURA_MAX.toInt()} °C"
        } else {
            null
        }
    }

    /** Si el equipo no está operativo, describir el problema es obligatorio. */
    fun observaciones(valor: String, estado: EstadoEquipo?): String? {
        val texto = valor.trim()
        return when {
            texto.length > OBSERVACIONES_MAX -> "Máximo $OBSERVACIONES_MAX caracteres"
            estado != null && estado != EstadoEquipo.OPERATIVO && texto.length < OBSERVACIONES_MIN ->
                "Describe el problema (mínimo $OBSERVACIONES_MIN caracteres)"
            else -> null
        }
    }

    fun foto(uri: String?): String? =
        if (uri.isNullOrBlank()) "La foto de evidencia es obligatoria" else null

    fun ubicacion(latitud: Double?, longitud: Double?): String? =
        if (latitud == null || longitud == null) "Debes registrar la ubicación de la visita" else null

    fun confirmacion(aceptada: Boolean): String? =
        if (!aceptada) "Debes confirmar que la información es verídica" else null

    fun motivoRechazo(valor: String): String? = when {
        valor.isBlank() -> "Ingresa el motivo del rechazo"
        valor.trim().length < MOTIVO_MIN -> "El motivo debe tener al menos $MOTIVO_MIN caracteres"
        else -> null
    }

    fun textoRequerido(valor: String, campo: String, min: Int = 3, max: Int = 60): String? {
        val texto = valor.trim()
        return when {
            texto.isEmpty() -> "Ingresa $campo"
            texto.length < min -> "Mínimo $min caracteres"
            texto.length > max -> "Máximo $max caracteres"
            else -> null
        }
    }

    fun latitud(valor: String): String? = coordenada(valor, -90.0, 90.0, "latitud")

    fun longitud(valor: String): String? = coordenada(valor, -180.0, 180.0, "longitud")

    private fun coordenada(valor: String, min: Double, max: Double, campo: String): String? {
        if (valor.isBlank()) return "Ingresa la $campo"
        val numero = valor.replace(',', '.').toDoubleOrNull() ?: return "Ingresa un número válido"
        return if (numero < min || numero > max) "La $campo debe estar entre ${min.toInt()} y ${max.toInt()}" else null
    }

    fun codigoActivo(valor: String): String? = when {
        valor.isBlank() -> "Ingresa el código del activo"
        !REGEX_CODIGO_ACTIVO.matches(valor.trim().uppercase()) -> "Formato esperado: TDV-VF-001 o TDV-CS-001"
        else -> null
    }
}
