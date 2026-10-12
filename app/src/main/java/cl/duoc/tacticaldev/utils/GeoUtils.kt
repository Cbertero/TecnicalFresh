package cl.duoc.tacticaldev.utils

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Cálculos geográficos para verificar la presencia del operario. */
object GeoUtils {

    /** Distancia máxima (metros) al punto de venta para considerar la visita verificada. */
    const val RADIO_VERIFICACION_METROS = 150.0

    private const val RADIO_TIERRA_METROS = 6_371_000.0

    /** Distancia en metros entre dos coordenadas usando la fórmula de Haversine. */
    fun distanciaMetros(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return RADIO_TIERRA_METROS * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    fun estaDentroDelRadio(distanciaMetros: Double): Boolean = distanciaMetros <= RADIO_VERIFICACION_METROS
}
