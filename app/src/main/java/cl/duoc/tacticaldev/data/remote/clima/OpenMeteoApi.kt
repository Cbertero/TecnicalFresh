package cl.duoc.tacticaldev.data.remote.clima

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API EXTERNA: Open-Meteo (https://open-meteo.com), clima gratuito y sin clave.
 * Es independiente del microservicio propio: usa su propio Retrofit y su propia URL.
 *
 * Ejemplo: GET https://api.open-meteo.com/v1/forecast?latitude=-33.51&longitude=-70.71
 *          &current=temperature_2m,relative_humidity_2m,weather_code&timezone=auto
 */
interface OpenMeteoApi {

    @GET("v1/forecast")
    suspend fun obtenerClimaActual(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("current") variables: String = "temperature_2m,relative_humidity_2m,weather_code",
        @Query("timezone") zonaHoraria: String = "auto"
    ): RespuestaClima
}

/** Respuesta de Open-Meteo. Solo usamos el bloque "current". */
data class RespuestaClima(
    @SerializedName("current") val actual: ClimaActualDto?
)

/** @SerializedName conecta el nombre del JSON (temperature_2m) con el nombre en Kotlin. */
data class ClimaActualDto(
    @SerializedName("temperature_2m") val temperatura: Double,
    @SerializedName("relative_humidity_2m") val humedad: Int,
    @SerializedName("weather_code") val codigo: Int
)
