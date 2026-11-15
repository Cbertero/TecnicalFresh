package cl.duoc.tacticaldev.data.remote.clima

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Cliente Retrofit SOLO para la API externa (separado de RetrofitInstance del microservicio). */
object OpenMeteoInstance {
    private const val BASE_URL = "https://api.open-meteo.com/"

    val api: OpenMeteoApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenMeteoApi::class.java)
    }
}
