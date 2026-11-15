package cl.duoc.tacticaldev.model

/** Clima actual en la ubicación del punto de venta (obtenido de Open-Meteo). */
data class Clima(
    val temperatura: Double,
    val humedad: Int,
    val descripcion: String
) {
    /** Sobre 25 °C los equipos de frío trabajan más exigidos. */
    val esDiaCaluroso: Boolean get() = temperatura >= TEMPERATURA_CALUROSA

    companion object {
        const val TEMPERATURA_CALUROSA = 25.0
    }
}

/** Traduce el código meteorológico WMO que entrega Open-Meteo a un texto en español. */
fun descripcionClima(codigo: Int): String = when (codigo) {
    0 -> "Despejado"
    1, 2 -> "Parcialmente nublado"
    3 -> "Nublado"
    45, 48 -> "Niebla"
    51, 53, 55, 56, 57 -> "Llovizna"
    61, 63, 65, 66, 67 -> "Lluvia"
    71, 73, 75, 77 -> "Nieve"
    80, 81, 82 -> "Chubascos"
    85, 86 -> "Chubascos de nieve"
    95, 96, 99 -> "Tormenta"
    else -> "Condición desconocida"
}
