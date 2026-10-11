package cl.duoc.tacticaldev.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Formatos de presentación reutilizables. */
object Formatos {
    private val localeChile = Locale("es", "CL")

    fun fechaHora(millis: Long): String =
        SimpleDateFormat("dd-MM-yyyy HH:mm", localeChile).format(Date(millis))

    fun distancia(metros: Double): String =
        if (metros < 1000) "${metros.toInt()} m" else String.format(localeChile, "%.1f km", metros / 1000)
}
