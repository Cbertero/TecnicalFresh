package cl.duoc.tacticaldev.viewmodel

import androidx.activity.ComponentActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.tacticaldev.TacticalApp
import cl.duoc.tacticaldev.data.remote.ApiService
import cl.duoc.tacticaldev.data.remote.RetrofitInstance
import cl.duoc.tacticaldev.data.remote.clima.OpenMeteoApi
import cl.duoc.tacticaldev.data.remote.clima.OpenMeteoInstance
import cl.duoc.tacticaldev.data.repository.VisitaRepository
import cl.duoc.tacticaldev.navigation.Screen
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

/**
 * Fábrica de ViewModels: debe poder crear cada ViewModel de la app con los
 * repositorios del AppContainer. Las APIs (microservicio y Open-Meteo) se
 * simulan para que la prueba nunca llame a internet.
 */
@RunWith(AndroidJUnit4::class)
@Config(application = TacticalApp::class)
class AppViewModelProviderTest {

    private lateinit var extras: CreationExtras

    @Before
    fun preparar() {
        mockkObject(RetrofitInstance, OpenMeteoInstance)
        // Mocks "estrictos": cualquier llamada lanza una excepción, como si no hubiera internet
        every { RetrofitInstance.api } returns mockk<ApiService>()
        every { OpenMeteoInstance.api } returns mockk<OpenMeteoApi>()

        // Una Activity real entrega lo que necesita SavedStateHandle (argumentos de navegación)
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()
        extras = MutableCreationExtras(activity.defaultViewModelCreationExtras).apply {
            set(ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY, ApplicationProvider.getApplicationContext<TacticalApp>())
            set(
                androidx.lifecycle.DEFAULT_ARGS_KEY,
                bundleOf(Screen.ARG_ACTIVO_ID to 1L, Screen.ARG_VISITA_ID to VisitaRepository.ID_EN_COLA)
            )
        }
    }

    @After
    fun limpiar() {
        unmockkObject(RetrofitInstance, OpenMeteoInstance)
    }

    private inline fun <reified T : ViewModel> crear(): T =
        AppViewModelProvider.Factory.create(T::class.java, extras)

    @Test
    fun creaLosViewModelsDeSesionYLogin() {
        crear<SesionViewModel>().shouldBeInstanceOf<SesionViewModel>()
        crear<LoginViewModel>().shouldBeInstanceOf<LoginViewModel>()
        crear<RegistroViewModel>().shouldBeInstanceOf<RegistroViewModel>()
    }

    @Test
    fun creaLosViewModelsDelSupervisor() {
        crear<SupervisorViewModel>().shouldBeInstanceOf<SupervisorViewModel>()
        crear<AdminViewModel>().shouldBeInstanceOf<AdminViewModel>()
        crear<TecnicoViewModel>().shouldBeInstanceOf<TecnicoViewModel>()
    }

    @Test
    fun app_obtieneLaAplicacionDesdeLosExtras() {
        extras.app() shouldBeSameInstanceAs ApplicationProvider.getApplicationContext<TacticalApp>()
    }
}
