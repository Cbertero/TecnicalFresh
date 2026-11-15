package cl.duoc.tacticaldev.viewmodel

import android.location.Location
import androidx.lifecycle.SavedStateHandle
import cl.duoc.tacticaldev.data.local.entity.VisitaEntity
import cl.duoc.tacticaldev.data.location.UbicacionService
import cl.duoc.tacticaldev.data.preferences.Sesion
import cl.duoc.tacticaldev.data.preferences.SesionPreferences
import cl.duoc.tacticaldev.data.repository.ActivoRepository
import cl.duoc.tacticaldev.data.repository.ClimaRepository
import cl.duoc.tacticaldev.model.Clima
import cl.duoc.tacticaldev.data.repository.VisitaRepository
import cl.duoc.tacticaldev.model.EstadoEquipo
import cl.duoc.tacticaldev.model.TipoVisita
import cl.duoc.tacticaldev.testutil.DatosPrueba
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow

/** Formulario de visita: validaciones, GPS y guardado. Es el corazón de la app. */
class VisitaViewModelTest : FunSpec({

    // El activo está en la latitud/longitud del Minimarket El Alba
    val activo = DatosPrueba.activoConPunto(id = 1, latitud = -33.5160, longitud = -70.7170)

    val activoRepository: ActivoRepository = mockk {
        coEvery { obtenerActivo(1) } returns activo
    }
    val visitaRepository: VisitaRepository = mockk()
    val climaDelLocal = Clima(temperatura = 27.4, humedad = 35, descripcion = "Despejado")
    val climaRepository: ClimaRepository = mockk {
        coEvery { obtenerClima(any(), any()) } returns climaDelLocal
    }
    val ubicacionService: UbicacionService = mockk()
    val sesionFlow = MutableStateFlow<Sesion?>(DatosPrueba.sesion(usuarioId = 1))
    val sesionPreferences: SesionPreferences = mockk {
        every { sesion } returns sesionFlow
    }

    // SavedStateHandle es donde Navigation deja los argumentos de la ruta (activoId)
    fun crearViewModel() = VisitaViewModel(
        savedStateHandle = SavedStateHandle(mapOf("activoId" to 1L)),
        activoRepository = activoRepository,
        visitaRepository = visitaRepository,
        sesionPreferences = sesionPreferences,
        ubicacionService = ubicacionService,
        climaRepository = climaRepository
    )

    val viewModel = crearViewModel()

    fun ubicacion(lat: Double, lon: Double): Location = mockk {
        every { latitude } returns lat
        every { longitude } returns lon
    }

    fun llenarFormularioValido() {
        coEvery { ubicacionService.obtenerUbicacionActual() } returns ubicacion(-33.5160, -70.7170)
        viewModel.onTipoVisitaChange(TipoVisita.MANTENCION)
        viewModel.onEstadoEquipoChange(EstadoEquipo.OPERATIVO)
        viewModel.onTemperaturaChange("-18,5")
        viewModel.onObservacionesChange("  Limpieza de condensador  ")
        viewModel.onFotoCapturada("content://foto.jpg")
        viewModel.obtenerUbicacion()
        viewModel.onConfirmacionChange(true)
    }

    context("Carga inicial") {
        test("carga el activo desde el repositorio") {
            viewModel.uiState.value.cargando shouldBe false
            viewModel.uiState.value.activo shouldBe activo
        }
    }

    context("Clima (API externa)") {
        test("al abrir consulta el clima en la ubicación del local") {
            viewModel.uiState.value.clima shouldBe climaDelLocal
            viewModel.uiState.value.cargandoClima shouldBe false
            coVerify { climaRepository.obtenerClima(-33.5160, -70.7170) }
        }

        test("si la API falla el clima queda vacío y se puede reintentar") {
            coEvery { climaRepository.obtenerClima(any(), any()) } returns null
            val vm = crearViewModel()
            vm.uiState.value.clima.shouldBeNull()

            coEvery { climaRepository.obtenerClima(any(), any()) } returns climaDelLocal
            vm.actualizarClima()

            vm.uiState.value.clima shouldBe climaDelLocal
        }

        test("sin activo no consulta el clima") {
            coEvery { activoRepository.obtenerActivo(1) } returns null
            val vm = crearViewModel()

            vm.actualizarClima()

            vm.uiState.value.clima.shouldBeNull()
            coVerify(exactly = 1) { climaRepository.obtenerClima(any(), any()) } // solo el del viewModel inicial
        }
    }

    context("Cambios de campos") {
        test("actualizan el estado y limpian los errores") {
            viewModel.validarFormulario() // genera errores en todos los campos
            viewModel.onTipoVisitaChange(TipoVisita.FALLA)
            viewModel.onFotoCapturada("content://foto.jpg")

            val s = viewModel.uiState.value
            s.tipoVisita shouldBe TipoVisita.FALLA
            s.fotoUri shouldBe "content://foto.jpg"
            s.errores.tipoVisita.shouldBeNull()
            s.errores.foto.shouldBeNull()
        }

        test("la temperatura se valida dentro del rango") {
            viewModel.onTemperaturaChange("99")
            viewModel.uiState.value.errores.temperatura.shouldNotBeNull()

            viewModel.onTemperaturaChange("-18")
            viewModel.uiState.value.errores.temperatura.shouldBeNull()
        }

        test("un equipo con falla vuelve obligatorias las observaciones") {
            viewModel.onObservacionesChange("")
            viewModel.uiState.value.errores.observaciones.shouldBeNull()

            viewModel.onEstadoEquipoChange(EstadoEquipo.CON_FALLA)

            viewModel.uiState.value.errores.observaciones.shouldNotBeNull()
        }

        test("no confirmar la información muestra un error") {
            viewModel.onConfirmacionChange(false)
            viewModel.uiState.value.errores.confirmacion.shouldNotBeNull()

            viewModel.onConfirmacionChange(true)
            viewModel.uiState.value.confirmado shouldBe true
            viewModel.uiState.value.errores.confirmacion.shouldBeNull()
        }

        test("denegar el permiso de ubicación muestra un error") {
            viewModel.onPermisoUbicacionDenegado()
            viewModel.uiState.value.errores.ubicacion shouldBe
                "Se necesita el permiso de ubicación para verificar la visita"
        }
    }

    context("GPS") {
        test("cerca del local la ubicación queda verificada") {
            coEvery { ubicacionService.obtenerUbicacionActual() } returns ubicacion(-33.5161, -70.7171)

            viewModel.obtenerUbicacion()

            val s = viewModel.uiState.value
            s.ubicacionVerificada shouldBe true
            s.distanciaMetros!! shouldBeLessThan 150.0
            s.latitud shouldBe -33.5161
            s.obteniendoUbicacion shouldBe false
        }

        test("lejos del local la ubicación no queda verificada") {
            // Plaza de Armas de Santiago: varios km del local
            coEvery { ubicacionService.obtenerUbicacionActual() } returns ubicacion(-33.4378, -70.6505)

            viewModel.obtenerUbicacion()

            viewModel.uiState.value.ubicacionVerificada shouldBe false
            viewModel.uiState.value.distanciaMetros!! shouldBeGreaterThan 1000.0
        }

        test("si el GPS no responde muestra un error") {
            coEvery { ubicacionService.obtenerUbicacionActual() } returns null

            viewModel.obtenerUbicacion()

            viewModel.uiState.value.errores.ubicacion.shouldNotBeNull()
            viewModel.uiState.value.obteniendoUbicacion shouldBe false
        }

        test("sin un activo cargado no consulta el GPS") {
            coEvery { activoRepository.obtenerActivo(1) } returns null
            val vm = crearViewModel()

            vm.obtenerUbicacion()

            coVerify(exactly = 0) { ubicacionService.obtenerUbicacionActual() }
        }
    }

    context("Guardar") {
        test("con el formulario incompleto muestra un mensaje y no guarda") {
            var guardada = false

            viewModel.guardar { guardada = true }

            val s = viewModel.uiState.value
            guardada shouldBe false
            s.mensaje shouldBe "Revisa los campos marcados en rojo"
            s.intentosFallidos shouldBe 1
            s.errores.hayErrores shouldBe true
            coVerify(exactly = 0) { visitaRepository.registrar(any()) }
        }

        test("con el formulario válido registra la visita con los datos correctos") {
            val visita = slot<VisitaEntity>()
            coEvery { visitaRepository.registrar(capture(visita)) } returns 77L
            llenarFormularioValido()
            var idGuardado: Long? = null

            viewModel.guardar { idGuardado = it }

            idGuardado shouldBe 77L
            with(visita.captured) {
                activoId shouldBe 1L
                usuarioId shouldBe 1L
                tipoVisita shouldBe "MANTENCION"
                estadoEquipo shouldBe "OPERATIVO"
                temperatura shouldBe -18.5 // la coma se convierte en punto
                observaciones shouldBe "Limpieza de condensador" // sin espacios extra
                ubicacionVerificada shouldBe true
            }
            viewModel.uiState.value.guardando shouldBe false
        }

        test("con la sesión expirada avisa y no guarda") {
            sesionFlow.value = null
            llenarFormularioValido()

            viewModel.guardar { }

            viewModel.uiState.value.mensaje shouldBe "La sesión expiró, vuelve a ingresar"
            coVerify(exactly = 0) { visitaRepository.registrar(any()) }
        }

        test("sin internet avisa que no se pudo guardar y quita la carga") {
            coEvery { visitaRepository.registrar(any()) } throws java.io.IOException("Sin conexión")
            llenarFormularioValido()
            var guardada = false

            viewModel.guardar { guardada = true }

            guardada shouldBe false
            viewModel.uiState.value.guardando shouldBe false
            viewModel.uiState.value.mensaje shouldBe "No se pudo guardar la visita. Revisa tu conexión a internet"
        }

        test("onMensajeMostrado limpia el mensaje") {
            viewModel.guardar { } // genera un mensaje
            viewModel.onMensajeMostrado()
            viewModel.uiState.value.mensaje.shouldBeNull()
        }
    }
})
