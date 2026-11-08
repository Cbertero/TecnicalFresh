package cl.duoc.tacticaldev.viewmodel

import cl.duoc.tacticaldev.data.preferences.Sesion
import cl.duoc.tacticaldev.data.preferences.SesionPreferences
import cl.duoc.tacticaldev.data.repository.UsuarioRepository
import cl.duoc.tacticaldev.model.Rol
import cl.duoc.tacticaldev.navigation.NavigationEvent
import cl.duoc.tacticaldev.testutil.DatosPrueba
import cl.duoc.tacticaldev.testutil.recolectar
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest

/**
 * Pruebas de ViewModels. Idea central: el ViewModel recibe sus dependencias por
 * el constructor, así que en la prueba le pasamos mocks en vez de los repositorios
 * reales. Luego llamamos sus funciones (como si el usuario escribiera o tocara un
 * botón) y revisamos `uiState.value`.
 *
 * El hilo principal (Dispatchers.Main) lo reemplaza ProjectConfig antes de todas las pruebas.
 */
class LoginViewModelTest : FunSpec({

    val usuarioRepository: UsuarioRepository = mockk()
    val sesionPreferences: SesionPreferences = mockk {
        coEvery { guardar(any()) } just Runs
    }
    val viewModel = LoginViewModel(usuarioRepository, sesionPreferences)

    test("onCorreoChange valida en tiempo real") {
        viewModel.onCorreoChange("mal-correo")
        viewModel.uiState.value.errores.correo.shouldNotBeNull()

        viewModel.onCorreoChange("tecnico@demo.cl")
        viewModel.uiState.value.correo shouldBe "tecnico@demo.cl"
        viewModel.uiState.value.errores.correo.shouldBeNull()
    }

    test("onClaveChange valida el largo mínimo") {
        viewModel.onClaveChange("123")
        viewModel.uiState.value.errores.clave.shouldNotBeNull()

        viewModel.onClaveChange("123456")
        viewModel.uiState.value.errores.clave.shouldBeNull()
    }

    test("alternarVisibilidadClave cambia entre visible y oculta") {
        viewModel.uiState.value.claveVisible shouldBe false
        viewModel.alternarVisibilidadClave()
        viewModel.uiState.value.claveVisible shouldBe true
        viewModel.alternarVisibilidadClave()
        viewModel.uiState.value.claveVisible shouldBe false
    }

    test("validarFormulario vacío marca errores y cuenta el intento") {
        viewModel.validarFormulario() shouldBe false
        viewModel.uiState.value.errores.correo.shouldNotBeNull()
        viewModel.uiState.value.errores.clave.shouldNotBeNull()
        viewModel.uiState.value.intentosFallidos shouldBe 1
    }

    test("iniciarSesion con el formulario inválido no consulta la base de datos") {
        var exito = false
        viewModel.iniciarSesion { exito = true }

        exito shouldBe false
        coVerify(exactly = 0) { usuarioRepository.autenticar(any(), any()) }
    }

    test("iniciarSesion con credenciales incorrectas muestra un error") {
        coEvery { usuarioRepository.autenticar(any(), any()) } returns null
        viewModel.onCorreoChange("tecnico@demo.cl")
        viewModel.onClaveChange("incorrecta")

        viewModel.iniciarSesion { }

        val estado = viewModel.uiState.value
        estado.errorGeneral shouldBe "Correo o clave incorrectos"
        estado.cargando shouldBe false
        estado.intentosFallidos shouldBe 1
    }

    test("iniciarSesion correcto guarda la sesión y navega según el rol") {
        val supervisora = DatosPrueba.usuario(id = 4, correo = "supervisor@demo.cl", rol = "SUPERVISOR")
        coEvery { usuarioRepository.autenticar("supervisor@demo.cl", "Clave123") } returns supervisora
        viewModel.onCorreoChange("supervisor@demo.cl")
        viewModel.onClaveChange("Clave123")

        var rolRecibido: Rol? = null
        viewModel.iniciarSesion { rolRecibido = it }

        rolRecibido shouldBe Rol.SUPERVISOR
        coVerify { sesionPreferences.guardar(Sesion(4, supervisora.nombre, Rol.SUPERVISOR, supervisora.cargo)) }
        viewModel.uiState.value.correo shouldBe "" // el formulario se limpia
    }
})

class RegistroViewModelTest : FunSpec({

    val usuarioRepository: UsuarioRepository = mockk()
    val viewModel = RegistroViewModel(usuarioRepository)

    fun llenarFormularioValido() {
        viewModel.onNombreChange("Ana Pérez")
        viewModel.onCorreoChange("ana@demo.cl")
        viewModel.onClaveChange("secreta1")
        viewModel.onConfirmarClaveChange("secreta1")
    }

    test("los cambios de campos actualizan el estado") {
        llenarFormularioValido()
        viewModel.onCargoChange("Vendedora")
        viewModel.toggleClaveVisible()

        val s = viewModel.uiState.value
        s.nombre shouldBe "Ana Pérez"
        s.correo shouldBe "ana@demo.cl"
        s.clave shouldBe "secreta1"
        s.confirmarClave shouldBe "secreta1"
        s.cargo shouldBe "Vendedora"
        s.claveVisible shouldBe true
    }

    test("onRolChange ajusta el cargo por defecto") {
        viewModel.onRolChange(Rol.SUPERVISOR)
        viewModel.uiState.value.cargo shouldBe "Supervisora de operaciones"

        viewModel.onRolChange(Rol.OPERARIO)
        viewModel.uiState.value.cargo shouldBe "Técnico de mantenimiento"
    }

    test("registrar con el formulario vacío muestra todos los errores") {
        viewModel.registrar { }

        val s = viewModel.uiState.value
        s.errorNombre.shouldNotBeNull()
        s.errorCorreo.shouldNotBeNull()
        s.errorClave.shouldNotBeNull()
        s.errorConfirmarClave shouldBe "Confirma tu clave"
        coVerify(exactly = 0) { usuarioRepository.registrar(any(), any(), any(), any(), any()) }
    }

    test("registrar con claves distintas muestra un error") {
        llenarFormularioValido()
        viewModel.onConfirmarClaveChange("otraClave")

        viewModel.registrar { }

        viewModel.uiState.value.errorConfirmarClave shouldBe "Las contraseñas no coinciden"
    }

    test("registrar exitoso marca el registro y avisa") {
        coEvery { usuarioRepository.registrar(any(), any(), any(), any(), any()) } returns Result.success(9L)
        llenarFormularioValido()
        var avisado = false

        viewModel.registrar { avisado = true }

        avisado shouldBe true
        viewModel.uiState.value.registroExitoso shouldBe true
        viewModel.uiState.value.cargando shouldBe false
        coVerify {
            usuarioRepository.registrar("Ana Pérez", "ana@demo.cl", "secreta1", "OPERARIO", "Técnico de mantenimiento")
        }
    }

    test("registrar un correo repetido muestra el mensaje del repositorio") {
        coEvery { usuarioRepository.registrar(any(), any(), any(), any(), any()) } returns
            Result.failure(Exception("Ya existe una cuenta con este correo"))
        llenarFormularioValido()

        viewModel.registrar { }

        viewModel.uiState.value.errorGeneral shouldBe "Ya existe una cuenta con este correo"
        viewModel.uiState.value.registroExitoso shouldBe false
    }

    test("un error sin mensaje muestra un mensaje genérico") {
        coEvery { usuarioRepository.registrar(any(), any(), any(), any(), any()) } returns Result.failure(Exception())
        llenarFormularioValido()

        viewModel.registrar { }

        viewModel.uiState.value.errorGeneral shouldBe "Error al registrar"
    }
})

class SesionViewModelTest : FunSpec({

    val sesionFlow = MutableStateFlow<Sesion?>(null)
    val sesionPreferences: SesionPreferences = mockk {
        every { sesion } returns sesionFlow
        coEvery { cerrar() } just Runs
    }

    test("sin sesión guardada el estado es SinSesion") {
        SesionViewModel(sesionPreferences).estado.value shouldBe EstadoSesion.SinSesion
    }

    test("con una sesión guardada el estado es Activa") {
        val viewModel = SesionViewModel(sesionPreferences)
        val sesion = DatosPrueba.sesion()

        sesionFlow.value = sesion

        viewModel.estado.value shouldBe EstadoSesion.Activa(sesion)
    }

    test("cerrarSesion borra los datos y avisa") {
        val viewModel = SesionViewModel(sesionPreferences)
        var terminado = false

        viewModel.cerrarSesion { terminado = true }

        terminado shouldBe true
        coVerify { sesionPreferences.cerrar() }
    }
})

class NavigationViewModelTest : FunSpec({

    test("emite los eventos de navegación en orden") {
        // runTest entrega un TestScope con `backgroundScope`, que usa recolectar()
        runTest {
            val viewModel = NavigationViewModel()
            val eventos = recolectar(viewModel.eventos)

            viewModel.navegarA("dashboard", limpiarPila = true)
            viewModel.cambiarPestana("mis_visitas", "inicio_operario")
            viewModel.volver()

            eventos shouldContainExactly listOf(
                NavigationEvent.NavigateTo("dashboard", null, false, true),
                NavigationEvent.NavigateTab("mis_visitas", "inicio_operario"),
                NavigationEvent.PopBackStack
            )
        }
    }
})
