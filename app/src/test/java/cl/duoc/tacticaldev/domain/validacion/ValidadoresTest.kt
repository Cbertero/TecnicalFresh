package cl.duoc.tacticaldev.domain.validacion

import cl.duoc.tacticaldev.model.EstadoEquipo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pruebas unitarias de las reglas de validación centralizadas.
 * Se ejecutan en la JVM (sin emulador): ./gradlew test
 */
class ValidadoresTest {

    // ---------- Login ----------

    @Test
    fun correo_vacio_devuelveError() {
        assertEquals("Ingresa tu correo", Validadores.correo(""))
    }

    @Test
    fun correo_sinArroba_devuelveError() {
        assertNotNull(Validadores.correo("tecnico.demo.cl"))
    }

    @Test
    fun correo_valido_noDevuelveError() {
        assertNull(Validadores.correo("tecnico@demo.cl"))
    }

    @Test
    fun clave_corta_devuelveError() {
        assertNotNull(Validadores.clave("12345"))
    }

    @Test
    fun clave_conLargoMinimo_esValida() {
        assertNull(Validadores.clave("123456"))
    }

    // ---------- Formulario de visita ----------

    @Test
    fun seleccion_nula_devuelveError() {
        assertEquals("Selecciona el tipo de visita", Validadores.seleccion(null, "el tipo de visita"))
    }

    @Test
    fun temperatura_noNumerica_devuelveError() {
        assertEquals("Ingresa un número válido", Validadores.temperatura("frío"))
    }

    @Test
    fun temperatura_fueraDeRango_devuelveError() {
        assertNotNull(Validadores.temperatura("25"))
        assertNotNull(Validadores.temperatura("-40"))
    }

    @Test
    fun temperatura_conComaDecimal_esValida() {
        assertNull(Validadores.temperatura("-18,5"))
    }

    @Test
    fun temperatura_enLimites_esValida() {
        assertNull(Validadores.temperatura("-30"))
        assertNull(Validadores.temperatura("15"))
    }

    @Test
    fun observaciones_equipoOperativo_puedenIrVacias() {
        assertNull(Validadores.observaciones("", EstadoEquipo.OPERATIVO))
    }

    @Test
    fun observaciones_equipoConFalla_sonObligatorias() {
        assertNotNull(Validadores.observaciones("No enfría", EstadoEquipo.CON_FALLA))
        assertNull(Validadores.observaciones("No enfría, compresor detenido", EstadoEquipo.CON_FALLA))
    }

    @Test
    fun observaciones_muyLargas_devuelveError() {
        assertNotNull(Validadores.observaciones("a".repeat(501), EstadoEquipo.OPERATIVO))
    }

    @Test
    fun foto_sinUri_devuelveError() {
        assertNotNull(Validadores.foto(null))
        assertNotNull(Validadores.foto(""))
        assertNull(Validadores.foto("content://cl.duoc.tacticaldev.fileprovider/evidencias/visita.jpg"))
    }

    @Test
    fun ubicacion_incompleta_devuelveError() {
        assertNotNull(Validadores.ubicacion(null, null))
        assertNotNull(Validadores.ubicacion(-33.45, null))
        assertNull(Validadores.ubicacion(-33.45, -70.66))
    }

    @Test
    fun confirmacion_noAceptada_devuelveError() {
        assertNotNull(Validadores.confirmacion(false))
        assertNull(Validadores.confirmacion(true))
    }

    // ---------- Supervisor ----------

    @Test
    fun motivoRechazo_corto_devuelveError() {
        assertNotNull(Validadores.motivoRechazo("Foto mala"))
        assertNull(Validadores.motivoRechazo("La foto no muestra el equipo"))
    }

    // ---------- Administración ----------

    @Test
    fun codigoActivo_formatoIncorrecto_devuelveError() {
        assertNotNull(Validadores.codigoActivo("VF-001"))
        assertNotNull(Validadores.codigoActivo("TDV-XX-001"))
    }

    @Test
    fun codigoActivo_enMinusculas_seAceptaNormalizado() {
        assertNull(Validadores.codigoActivo("tdv-vf-004"))
    }

    @Test
    fun coordenadas_fueraDeRango_devuelveError() {
        assertNotNull(Validadores.latitud("-95"))
        assertNotNull(Validadores.longitud("200"))
        assertNull(Validadores.latitud("-33.4489"))
        assertNull(Validadores.longitud("-70.6693"))
    }

    @Test
    fun textoRequerido_respetaLimites() {
        assertEquals("Ingresa el nombre", Validadores.textoRequerido("   ", "el nombre"))
        assertNotNull(Validadores.textoRequerido("ab", "el nombre"))
        assertNull(Validadores.textoRequerido("Minimarket Sol", "el nombre"))
    }
}
