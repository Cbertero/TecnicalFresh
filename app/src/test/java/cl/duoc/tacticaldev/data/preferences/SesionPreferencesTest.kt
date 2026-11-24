package cl.duoc.tacticaldev.data.preferences

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.tacticaldev.model.Rol
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Sesión guardada con DataStore Preferences. Con Robolectric el archivo se
 * escribe de verdad en una carpeta temporal, así que probamos el ciclo completo:
 * guardar → leer → cerrar sesión.
 */
@RunWith(AndroidJUnit4::class)
class SesionPreferencesTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val preferencias = SesionPreferences(context)

    @Before
    fun empezarSinSesion() = runBlocking { preferencias.cerrar() }

    @After
    fun limpiar() = runBlocking { preferencias.cerrar() }

    @Test
    fun sinDatosGuardados_noHaySesion() = runBlocking<Unit> {
        preferencias.sesion.first().shouldBeNull()
    }

    @Test
    fun guardar_permiteLeerLaMismaSesion() = runBlocking<Unit> {
        val sesion = Sesion(usuarioId = 4, nombre = "Carla Fuentes", rol = Rol.SUPERVISOR, cargo = "Supervisor de operaciones")

        preferencias.guardar(sesion)

        preferencias.sesion.first() shouldBe sesion
    }

    @Test
    fun guardar_sinCargo_loLeeComoTextoVacio() = runBlocking<Unit> {
        preferencias.guardar(Sesion(usuarioId = 1, nombre = "Tomás", rol = Rol.OPERARIO))

        val leida = preferencias.sesion.first()
        leida?.cargo shouldBe ""
        leida?.rol shouldBe Rol.OPERARIO
    }

    @Test
    fun cerrar_borraLaSesion() = runBlocking<Unit> {
        preferencias.guardar(Sesion(1, "Tomás", Rol.OPERARIO, "Técnico"))

        preferencias.cerrar()

        preferencias.sesion.first().shouldBeNull()
    }

    @Test
    fun unaSegundaInstanciaLeeLoMismo() = runBlocking<Unit> {
        // Las dos instancias comparten el mismo archivo (propiedad Context.dataStore)
        preferencias.guardar(Sesion(2, "Valentina", Rol.OPERARIO, "Vendedor"))

        SesionPreferences(context).sesion.first()?.nombre shouldBe "Valentina"
    }
}
