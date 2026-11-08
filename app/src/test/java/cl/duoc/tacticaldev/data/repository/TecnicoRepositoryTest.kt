package cl.duoc.tacticaldev.data.repository

import android.content.Context
import cl.duoc.tacticaldev.data.model.Tecnico
import cl.duoc.tacticaldev.data.remote.ApiService
import cl.duoc.tacticaldev.data.remote.RetrofitInstance
import cl.duoc.tacticaldev.testutil.FakeSharedPreferences
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.longs.shouldBeNegative
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import java.io.IOException

/**
 * Pruebas del repositorio que se conecta al microservicio en Railway.
 *
 * Dos trucos para probarlo sin internet ni teléfono:
 *  1. El Context es un mock que entrega un [FakeSharedPreferences] (memoria).
 *  2. `mockkObject(RetrofitInstance)` reemplaza el objeto singleton de Retrofit,
 *     así `RetrofitInstance.api` devuelve un ApiService simulado. Con él
 *     simulamos "hay internet" (responde datos) o "no hay internet" (lanza IOException).
 */
class TecnicoRepositoryTest : FunSpec({

    val prefs = FakeSharedPreferences()
    val contexto: Context = mockk {
        every { getSharedPreferences(any(), any()) } returns prefs
    }
    val api: ApiService = mockk()

    beforeTest {
        mockkObject(RetrofitInstance)
        every { RetrofitInstance.api } returns api
    }

    afterTest {
        unmockkAll() // deja RetrofitInstance como estaba para las demás pruebas
    }

    fun nuevoRepositorio() = TecnicoRepository(contexto)

    fun sinInternet() {
        coEvery { api.getTecnicos() } throws IOException("Sin conexión")
        coEvery { api.guardarTecnico(any()) } throws IOException("Sin conexión")
        coEvery { api.eliminarTecnico(any()) } throws IOException("Sin conexión")
    }

    context("Lectura") {
        test("al crearse sin datos guardados la lista está vacía") {
            nuevoRepositorio().asignacionesFlow.value.shouldBeEmpty()
        }

        test("con internet marca los registros como sincronizados y los guarda en caché") {
            coEvery { api.getTecnicos() } returns listOf(
                Tecnico(id = 1, nombre = "Ana", cargo = "Vendedora", localAsignado = null, sincronizado = false)
            )
            val repo = nuevoRepositorio()

            val lista = repo.geTecnicos()

            lista shouldHaveSize 1
            lista[0].sincronizado shouldBe true
            lista[0].localAsignado shouldBe "" // null se normaliza a ""
            repo.asignacionesFlow.value shouldBe lista
            // Un repositorio nuevo (como si se reabriera la app) lee el caché guardado
            nuevoRepositorio().asignacionesFlow.value shouldHaveSize 1
        }

        test("sin internet devuelve lo que hay guardado en el teléfono") {
            // Primero hay internet y se llena el caché...
            coEvery { api.getTecnicos() } returns listOf(Tecnico(1, "Ana", "Vendedora", "Local A"))
            nuevoRepositorio().geTecnicos()
            // ...luego se corta la conexión
            sinInternet()

            nuevoRepositorio().geTecnicos().map { it.nombre } shouldContainExactly listOf("Ana")
        }

        test("un caché corrupto se ignora y no revienta la app") {
            prefs.edit().putString("cache_servidor", "esto no es JSON").apply()
            prefs.edit().putString("cola_pendientes", "{roto").apply()

            nuevoRepositorio().asignacionesFlow.value.shouldBeEmpty()
        }
    }

    context("Guardar") {
        test("con internet devuelve el registro del servidor") {
            coEvery { api.guardarTecnico(any()) } returns Tecnico(5, "Ana", "Vendedora", null)
            coEvery { api.getTecnicos() } returns listOf(Tecnico(5, "Ana", "Vendedora", "Local A"))
            val repo = nuevoRepositorio()

            val guardado = repo.GuardarTecnico("Ana", "Vendedora", "Local A")

            guardado.id shouldBe 5L
            guardado.localAsignado shouldBe "Local A"
            guardado.sincronizado shouldBe true
            repo.asignacionesFlow.value shouldHaveSize 1 // refrescó la lista
        }

        test("sin internet queda pendiente con un id temporal negativo") {
            sinInternet()
            val repo = nuevoRepositorio()

            val pendiente = repo.GuardarTecnico("Ana", "Vendedora", "Local A")

            pendiente.id!!.shouldBeNegative()
            pendiente.sincronizado shouldBe false
            repo.asignacionesFlow.value shouldContainExactly listOf(pendiente)
        }
    }

    context("Sincronización") {
        test("al volver internet sube los pendientes y vacía la cola") {
            sinInternet()
            val repo = nuevoRepositorio()
            repo.GuardarTecnico("Ana", "Vendedora", "Local A")

            // Vuelve internet
            coEvery { api.guardarTecnico(any()) } returns Tecnico(8, "Ana", "Vendedora", "Local A")
            coEvery { api.getTecnicos() } returns listOf(Tecnico(8, "Ana", "Vendedora", "Local A"))

            val lista = repo.geTecnicos()

            // Se envía SIN el id temporal negativo: el id real lo asigna MySQL
            coVerify {
                api.guardarTecnico(Tecnico(id = null, nombre = "Ana", cargo = "Vendedora", localAsignado = "Local A"))
            }
            lista.map { it.id } shouldContainExactly listOf(8L)
            lista.all { it.sincronizado } shouldBe true
        }

        test("si uno falla, ese permanece en la cola") {
            sinInternet()
            val repo = nuevoRepositorio()
            repo.GuardarTecnico("Ana", "Vendedora", "Local A")
            repo.GuardarTecnico("Luis", "Técnico", "Local B")

            // Internet intermitente: Ana se sube, Luis falla
            coEvery { api.guardarTecnico(match { it.nombre == "Ana" }) } returns Tecnico(1, "Ana", "Vendedora", "Local A")
            coEvery { api.guardarTecnico(match { it.nombre == "Luis" }) } throws IOException()

            repo.sincronizarPendientes()

            val pendientes = nuevoRepositorio().asignacionesFlow.value.filter { !it.sincronizado }
            pendientes.map { it.nombre } shouldContainExactly listOf("Luis")
        }

        test("sin pendientes no llama al servidor") {
            nuevoRepositorio().sincronizarPendientes()

            coVerify(exactly = 0) { api.guardarTecnico(any()) }
        }
    }

    context("Eliminar") {
        test("un id negativo solo se quita de la cola local") {
            sinInternet()
            val repo = nuevoRepositorio()
            val pendiente = repo.GuardarTecnico("Ana", "Vendedora", "Local A")

            repo.eliminarTecnico(pendiente.id!!)

            repo.asignacionesFlow.value.shouldBeEmpty()
            coVerify(exactly = 0) { api.eliminarTecnico(any()) }
        }

        test("un id positivo llama al servidor y se quita del caché") {
            coEvery { api.getTecnicos() } returns listOf(
                Tecnico(1, "Ana", "Vendedora", "Local A"),
                Tecnico(2, "Luis", "Técnico", "Local B")
            )
            coEvery { api.eliminarTecnico(1) } just Runs
            val repo = nuevoRepositorio()
            repo.geTecnicos()

            repo.eliminarTecnico(1)

            coVerify { api.eliminarTecnico(1) }
            repo.asignacionesFlow.value.map { it.id } shouldContainExactly listOf(2L)
        }

        test("un id positivo sin internet igual se quita del caché") {
            coEvery { api.getTecnicos() } returns listOf(Tecnico(1, "Ana", "Vendedora", "Local A"))
            val repo = nuevoRepositorio()
            repo.geTecnicos()
            sinInternet()

            repo.eliminarTecnico(1)

            repo.asignacionesFlow.value.shouldBeEmpty()
        }
    }
})
