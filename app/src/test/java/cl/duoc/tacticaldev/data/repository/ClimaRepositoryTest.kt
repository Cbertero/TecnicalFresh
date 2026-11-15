package cl.duoc.tacticaldev.data.repository

import cl.duoc.tacticaldev.data.remote.clima.ClimaActualDto
import cl.duoc.tacticaldev.data.remote.clima.OpenMeteoApi
import cl.duoc.tacticaldev.data.remote.clima.OpenMeteoInstance
import cl.duoc.tacticaldev.data.remote.clima.RespuestaClima
import cl.duoc.tacticaldev.model.Clima
import cl.duoc.tacticaldev.model.descripcionClima
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException

/** Pruebas de la API externa (Open-Meteo), simulando la respuesta con MockK. */
class ClimaRepositoryTest : FunSpec({

    val api: OpenMeteoApi = mockk()
    val repository = ClimaRepository(api)

    test("convierte la respuesta de Open-Meteo en un Clima con descripción en español") {
        coEvery { api.obtenerClimaActual(-33.5, -70.7) } returns
            RespuestaClima(ClimaActualDto(temperatura = 27.4, humedad = 35, codigo = 0))

        val clima = repository.obtenerClima(-33.5, -70.7)

        clima shouldBe Clima(temperatura = 27.4, humedad = 35, descripcion = "Despejado")
        clima!!.esDiaCaluroso shouldBe true
        // Pide las variables de clima actual a la API
        coVerify { api.obtenerClimaActual(-33.5, -70.7, "temperature_2m,relative_humidity_2m,weather_code", "auto") }
    }

    test("si la respuesta no trae el bloque current devuelve null") {
        coEvery { api.obtenerClimaActual(any(), any(), any(), any()) } returns RespuestaClima(null)

        repository.obtenerClima(0.0, 0.0).shouldBeNull()
    }

    test("sin internet devuelve null y no interrumpe el formulario") {
        coEvery { api.obtenerClimaActual(any(), any(), any(), any()) } throws IOException()

        repository.obtenerClima(0.0, 0.0).shouldBeNull()
    }

    test("un día templado no se marca como caluroso") {
        Clima(18.0, 60, "Nublado").esDiaCaluroso shouldBe false
        Clima(Clima.TEMPERATURA_CALUROSA, 60, "Despejado").esDiaCaluroso shouldBe true
    }

    test("traduce los códigos meteorológicos WMO") {
        mapOf(
            0 to "Despejado", 2 to "Parcialmente nublado", 3 to "Nublado", 45 to "Niebla",
            53 to "Llovizna", 63 to "Lluvia", 73 to "Nieve", 81 to "Chubascos",
            85 to "Chubascos de nieve", 95 to "Tormenta", 123 to "Condición desconocida"
        ).forEach { (codigo, texto) -> descripcionClima(codigo) shouldBe texto }
    }

    test("el cliente de Open-Meteo se crea sin conectarse a internet") {
        OpenMeteoInstance.api.shouldNotBeNull()
    }
})
